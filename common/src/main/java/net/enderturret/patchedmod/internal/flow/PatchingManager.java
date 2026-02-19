package net.enderturret.patchedmod.internal.flow;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.FallbackResourceManager.PackEntry;
import net.minecraft.server.packs.resources.IoSupplier;

import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.Patches;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.common.env.IPatchingPackResources;
import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.flow.BailException;
import net.enderturret.patchedmod.common.internal.flow.LazyPatchingWrapper;
import net.enderturret.patchedmod.common.util.PatchingInputStream;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.internal.PatchedTestEvaluator;
import net.enderturret.patchedmod.util.PatchUtil;
import net.enderturret.patchedmod.util.PatchedFileAccess;

/**
 * The {@code PatchingManager} class handles the overall management of patching files and setting up packs for patching.
 * @author EnderTurret
 */
@Internal
public final class PatchingManager {

	/**
	 * A global flag that allows turning on various debug messages from Patched, as well as the {@code debug} subcommand.
	 * The most noticeable effect is several log messages being raised from {@code DEBUG} to {@code INFO} -- this
	 * is because those messages are otherwise invisible on Fabric and Quilt.
	 */
	@Internal
	public static final boolean DEBUG = Boolean.getBoolean("patched.debug");

	// Whether to print the "patched:has_patches" deprecation warning.
	// This is here so it can be disabled on older versions.
	private static final boolean HASPATCHES_WARNING = true;

	private static final AtomicBoolean LOG_EXCEPTIONS = new AtomicBoolean(true);

	/**
	 * "Chains" the given {@code IoSupplier}, returning an {@code IoSupplier} that patches the data returned by it.
	 * @param delegate The delegate {@code IoSupplier}.
	 * @param manager The resource manager that the data is from.
	 * @param type The type of pack this data is from.
	 * @param name The location of the data.
	 * @param origin The resource or data pack that the data originated from.
	 * @param singlePack Whether or not only patches from the pack containing the resource should be applied.
	 * @return The new {@code IoSupplier}.
	 */
	@Internal
	public static IoSupplier<InputStream> chain(IoSupplier<InputStream> delegate, FallbackResourceManager manager, PackType type, Identifier name, PackResources origin, boolean singlePack) {
		if (!PatchUtil.isPatchable(name)) return delegate;

		return () -> new PatchingInputStream(delegate.get(), (stream, audit) -> patch(manager, origin, type, name, stream, audit, singlePack));
	}

	/**
	 * Patches the data from the given stream, returning the patched data as a stream.
	 * @param manager The resource manager that the data is from.
	 * @param from The resource or data pack that the data originated from.
	 * @param type The type of pack this data is from.
	 * @param name The location of the data.
	 * @param stream The data stream.
	 * @param audit The audit to record changes made by the patches.
	 * @param singlePack Whether or not only patches from the pack containing the resource should be applied.
	 * @return A new stream containing the patched data.
	 */
	private static InputStream patch(FallbackResourceManager manager, PackResources from, PackType type, Identifier name, InputStream stream, @Nullable PatchAudit audit, boolean singlePack) {
		if (stream == null || !PatchUtil.isPatchable(name)) return stream;

		final LazyPatchingWrapper wrapper = new LazyPatchingWrapper(stream);

		try {
			if (singlePack)
				patchSingle(manager, from, type, name, wrapper, audit);
			else
				patch(manager, from, type, name, wrapper, audit);
		} catch (BailException e) {
			// Let the future data consumer handle these.
		} catch (Exception e) {
			if (LOG_EXCEPTIONS.getAndSet(false))
				Patched.platform().logger().error("An exception occurred while attempting to patch {}. Further exceptions will not be reported.", name, e);
		}

		return wrapper.getOrCreateStream();
	}

	/**
	 * Patches the given stream using only patches from the source pack.
	 * @param manager The resource manager that the stream is from.
	 * @param from The resource or data pack that the stream originated from.
	 * @param type The type of pack this stream is from.
	 * @param name The location of the stream.
	 * @param wrapper The stream to patch.
	 * @param audit The audit to record changes made by the patches.
	 * @return Whether any patches were actually applied.
	 */
	@SuppressWarnings("resource")
	private static boolean patchSingle(FallbackResourceManager manager, PackResources from, PackType type, Identifier name, LazyPatchingWrapper wrapper, @Nullable PatchAudit audit) {
		// Since many packs could provide this file, we cannot rely on existence checks to find the real pack.
		// This will simply have to not work in that case.
		//from = findTrueSource(from, type, name);

		if (hasPatches(from)) {
			final Identifier patchName = name.withPath(name.getPath() + ".patch");
			final MutableObject<PatchContext> context = new MutableObject<>();

			applyPatch(
					type, from.getResource(type, patchName),
					patchName.toString(), new Entry(from), wrapper, audit, context,
					null
					);

			return context.get() != null;
		}

		return false;
	}

	/**
	 * Patches the given stream using patches from all of the packs with the given pack type.
	 * @param manager The resource manager that the stream is from.
	 * @param from The resource or data pack that the stream originated from.
	 * @param type The type of pack this stream is from.
	 * @param name The location of the stream.
	 * @param wrapper The stream to patch.
	 * @param audit The audit to record changes made by the patches.
	 * @return Whether any patches were actually applied.
	 */
	@SuppressWarnings("resource")
	private static boolean patch(FallbackResourceManager manager, PackResources from, PackType type, Identifier name, LazyPatchingWrapper wrapper, @Nullable PatchAudit audit) {
		final Identifier patchName = name.withPath(name.getPath() + ".patch");

		final MutableObject<PatchContext> context = new MutableObject<>();

		final Map<PackResources, List<String>> targets = DynamicPatches.getTargets(type == PackType.CLIENT_RESOURCES ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA, name, from);

		boolean seenOriginal = false;
		for (int i = 0; i < manager.fallbacks.size(); i++) {
			final PackEntry packEntry = manager.fallbacks.get(i);
			if (packEntry.resources() == null) continue;

			// Until we see the pack the file originated from, don't apply any patches.
			if (!seenOriginal)
				if (packEntry.resources() == from)
					seenOriginal = true;
				else
					continue;

			if (hasPatches(packEntry.resources())) {
				final Entry pack = new Entry(packEntry);

				PatchContext ctx = applyPatch(
						type, pack.resources().getResource(type, patchName),
						patchName.toString(), pack, wrapper, audit, context,
						null
						);

				IFileAccess access = null;
				for (String patch : targets.getOrDefault(pack.resources(), List.of())) {
					// We use the IFileAccess instead of grabbing it manually so that it's cached.
					if (access == null)
						access = ctx != null ? ctx.fileAccess() : new PatchedFileAccess(pack.resources());

					applyPatch(
							type, access.readIncludedPatch(patch),
							"patches/" + patch + ".json.patch", pack, wrapper, audit, context,
							name.toString()
							);
				}
			}
		}

		return context.get() != null;
	}

	private static PatchContext applyPatch(
			PackType type,
			@Nullable IoSupplier<InputStream> patchSupplier,
			String patchName,
			Entry pack,
			LazyPatchingWrapper wrapper,
			@Nullable PatchAudit audit,
			MutableObject<PatchContext> context,
			String explicitTargetName) {
		if (patchSupplier == null) return null;

		final String patchJson;

		try (InputStream patchStream = patchSupplier.get()) {
			patchJson = PatchedInternal.readString(patchStream);
		} catch (Exception e) {
			Patched.platform().logger().warn("Failed to read patch {} from {}:", patchName, pack.name(), e);
			return null;
		}

		final JsonPatch patch;

		try {
			patch = Patches.readPatch(PatchedInternal.GSON, patchJson);
		} catch (Exception e) {
			Patched.platform().logger().warn("Failed to parse patch {} from {}:", patchName, pack.name(), e);
			return null;
		}

		return applyPatch(type, patch, patchName, pack, wrapper, audit, context, explicitTargetName);
	}

	private static PatchContext applyPatch(
			PackType type,
			JsonPatch patch,
			String patchName,
			Entry pack,
			LazyPatchingWrapper wrapper,
			@Nullable PatchAudit audit,
			MutableObject<PatchContext> context,
			String explicitTargetName) {
		try {
			if (audit != null)
				audit.setPatchPath(pack.name());
			if (context.get() == null)
				context.setValue(PatchedInternal.BASE_CONTEXT.audit(audit).testEvaluator(new PatchedTestEvaluator(type == PackType.CLIENT_RESOURCES ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA)));

			Patched.platform().logger().atLevel(DEBUG ? Level.INFO : Level.DEBUG).log("Applying patch {} from {}{}.",
					patchName,
					pack.name(),
					explicitTargetName != null ? " to " + explicitTargetName : "");

			final PatchContext ctx = context.get().fileAccess(new PatchedFileAccess(pack.resources()));

			patch.patch(wrapper.get(), ctx);

			return ctx;
		} catch (BailException e) {
			throw e;
		} catch (PatchingException e) {
			Patched.platform().logger().warn("Failed to apply patch {} from {}:\n{}", patchName, pack.name(), e.toString());
		} catch (Exception e) {
			Patched.platform().logger().warn("Failed to apply patch {} from {}:", patchName, pack.name(), e);
		}

		return null;
	}

	/**
	 * Determines whether the given pack has patches enabled.
	 * If necessary, the pack may be {@linkplain IPatchingPackResources#patched$initialized() initialized}.
	 * @param res The pack to check.
	 * @return {@code true} if the pack has patches enabled.
	 */
	private static boolean hasPatches(PackResources res) {
		return res instanceof IPatchingPackResources ppp && ppp.patchedMetadata().patchingEnabled();
	}

	/**
	 * Initializes the {@code PatchedMetadata} of the specified pack, if it has not been initialized yet.
	 * @param resources The pack to initialize.
	 */
	public static void maybeInitialize(PackResources resources) {
		maybeInitialize(new Entry(resources));
	}

	/**
	 * Initializes the {@code PatchedMetadata} of the specified pack, if it has not been initialized yet.
	 * @param resources The pack to initialize.
	 */
	public static void maybeInitialize(IPatchingPackResources resources) {
		maybeInitialize(new Entry((PackResources) resources));
	}

	/**
	 * Initializes the {@code PatchedMetadata} of the specified pack, if it has not been initialized yet.
	 * @param entry The pack to initialize.
	 */
	static void maybeInitialize(Entry entry) {
		if (!(entry.resources() instanceof PatchedPackResources patching))
			return;

		if (!patching.patched$initialized())
			synchronized (patching) {
				if (!patching.patched$initialized()) {
					{
						final IoSupplier<InputStream> io = entry.resources().getRootResource("pack.mcmeta");
						PatchedMetadata meta;

						if (io != null)
							try (InputStream is = io.get()) {
								final String json = PatchedInternal.readString(is);
								final JsonElement elem = JsonParser.parseString(json);

								meta = PatchedMetadata.of(elem, entry.name());
							} catch (Exception e) {
								Patched.platform().logger().warn("Failed to read pack.mcmeta in {}:", entry.name(), e);
								meta = PatchedMetadata.DISABLED_METADATA;
							}
						else
							meta = PatchedMetadata.DISABLED_METADATA;

						if (!meta.patchingEnabled())
							meta = Objects.requireNonNullElse(
									Patched.platform().deriveMetadataFromMod(patching),
									meta);

						patching.setPatchedMetadata(meta);
					}

					if (patching.patchedMetadata().patchingEnabled()) {
						if (patching.patchedMetadata().formatVersion() == 0) {
							if (HASPATCHES_WARNING)
								Patched.platform().logger().warn("Loaded legacy PatchedMetadata from {}. This behavior is deprecated and will be removed in Minecraft 26.1.", entry.name());
							else
								Patched.platform().logger().atLevel(DEBUG ? Level.INFO : Level.DEBUG).log("Loaded legacy PatchedMetadata from {}.", entry.name());
						} else
							Patched.platform().logger().atLevel(DEBUG ? Level.INFO : Level.DEBUG).log("Loaded PatchedMetadata from {} with format version {}.", entry.name(), patching.patchedMetadata().formatVersion());
					}
				}
			}
	}
}