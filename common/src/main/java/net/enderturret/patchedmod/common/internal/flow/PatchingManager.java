package net.enderturret.patchedmod.common.internal.flow;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.Patches;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.common.env.PatchingPackResources;
import net.enderturret.patchedmod.common.internal.PatchedFileAccess;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.PatchedTestEvaluator;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;
import net.enderturret.patchedmod.common.util.PatchTrace;
import net.enderturret.patchedmod.common.util.PatchUtil;
import net.enderturret.patchedmod.common.util.PatchingInputStream;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

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

	private static final boolean HAS_GROUP_PACKS = PatchedPlatform.get().hasGroupPacks();

	// Whether to print the "patched:has_patches" deprecation warning.
	// This is here so it can be disabled on older versions.
	private static final boolean HASPATCHES_WARNING = !PatchedPlatform.get().hasLegacyPatchedMetadata();

	private static final AtomicBoolean LOG_EXCEPTIONS = new AtomicBoolean(true);

	/**
	 * Creates a new {@link PatchingInputStream} from the specified {@link InputStream}.
	 * @param delegate The delegate {@code InputStream}.
	 * @param manager The resource manager that the data is from.
	 * @param origin The resource or data pack that the data originated from.
	 * @param type The type of pack this data is from.
	 * @param name The location of the data.
	 * @param singlePack Whether or not only patches from the pack containing the resource should be applied.
	 * @return The new {@code PatchingInputStream}.
	 * @throws IOException If an I/O error occurs.
	 */
	@Internal
	public static PatchingInputStream newPatchingStream(InputStream delegate, PatchedResourceManager manager, PatchedPackResources origin, PatchedPackType type, PatchedResourceLocation name, boolean singlePack) throws IOException {
		if (!manager.patched$isFallback()) throw new IllegalArgumentException("Expected fallback resource manager");
		return new PatchingInputStream(delegate, (stream, audit, trace) -> patch(manager, origin, type, name, stream, audit, trace, singlePack));
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
	@Internal
	private static InputStream patch(PatchedResourceManager manager, PatchedPackResources from, PatchedPackType type, PatchedResourceLocation name, InputStream stream, @Nullable PatchAudit audit, PatchTrace trace, boolean singlePack) {
		if (stream == null || !PatchUtil.isPatchable(name.patched$getPath())) return stream;

		final LazyPatchingWrapper wrapper = new LazyPatchingWrapper(stream);

		try {
			if (singlePack)
				patchSingle(manager, from, type, name, wrapper, audit, trace);
			else
				patch(manager, from, type, name, wrapper, audit, trace);
		} catch (BailException e) {
			// Let the future data consumer handle these.
		} catch (Exception e) {
			if (LOG_EXCEPTIONS.getAndSet(false))
				PatchedInternal.LOGGER.error("An exception occurred while attempting to patch {}. Further exceptions will not be reported.", name, e);
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
	private static boolean patchSingle(PatchedResourceManager manager, PatchedPackResources from, PatchedPackType type, PatchedResourceLocation name, LazyPatchingWrapper wrapper, @Nullable PatchAudit audit, PatchTrace trace) {
		// Since many packs could provide this file, we cannot rely on existence checks to find the real pack.
		// This will simply have to not work in that case.
		// (Only relevant on <1.21.1.)

		if (from.patched$hasPatches()) {
			final PatchedResourceLocation patchName = name.patched$withPath(name.patched$getPath() + ".patch");
			final PatchContext[] context = new PatchContext[1];

			try {
				applyPatch(
						type, from.patched$getResource(type, patchName),
						patchName.toString(), new Entry(from), wrapper, audit, trace, context,
						null
						);
			} catch (IOException e) {
				PatchedInternal.LOGGER.warn("Failed to read patch {} from {}:", patchName, from.patched$getName(), e);
			}

			return context[0] != null;
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
	private static boolean patch(PatchedResourceManager manager, PatchedPackResources from, PatchedPackType type, PatchedResourceLocation name, LazyPatchingWrapper wrapper, @Nullable PatchAudit audit, PatchTrace trace) {
		final PatchedResourceLocation patchName = name.patched$withPath(name.patched$getPath() + ".patch");

		final PatchContext[] context = new PatchContext[1];

		if (HAS_GROUP_PACKS)
			from = findTrueSource(from, type, name);

		final Map<PatchedPackResources, List<String>> targets = DynamicPatches.getTargets(type, name, from);

		boolean seenOriginal = false;
		final int size = manager.patched$getFallbackPackCount();
		for (int i = 0; i < size; i++) {
			final PatchedPackResources packEntry = manager.patched$getFallbackPack(i);
			if (packEntry == null) continue;

			if (HAS_GROUP_PACKS)
				for (Entry pack : packsIn(new Entry(packEntry), type, patchName.patched$getNamespace())) {
					// Until we see the pack the file originated from, don't apply any patches.
					if (!seenOriginal)
						if (pack.resources() == from) {
							trace.recordFile(pack.resources(), false);
							seenOriginal = true;
						} else {
							if (trace.active())
								traceOverridenPatchesOrFile(type, name, patchName, pack.resources(), trace, targets.getOrDefault(pack.resources(), List.of()));
							continue;
						}

					if (pack.resources().patched$hasPatches())
						applyPatchesFromPack(manager, from, pack, type, name, patchName, wrapper, audit, trace, context, targets);
				}

			else {
				// Until we see the pack the file originated from, don't apply any patches.
				if (!seenOriginal)
					if (packEntry == from) {
						trace.recordFile(packEntry, false);
						seenOriginal = true;
					} else {
						if (trace.active())
							traceOverridenPatchesOrFile(type, name, patchName, packEntry, trace, targets.getOrDefault(packEntry, List.of()));
						continue;
					}

				if (packEntry.patched$hasPatches())
					applyPatchesFromPack(manager, from, new Entry(packEntry), type, name, patchName, wrapper, audit, trace, context, targets);
			}
		}

		return context[0] != null;
	}

	private static void applyPatchesFromPack(
			PatchedResourceManager manager,
			PatchedPackResources from,
			Entry pack,
			PatchedPackType type,
			PatchedResourceLocation name,
			PatchedResourceLocation patchName,
			LazyPatchingWrapper wrapper,
			@Nullable PatchAudit audit,
			PatchTrace trace,
			PatchContext[] context,
			Map<PatchedPackResources, List<String>> targets) {
		PatchContext ctx = null;

		try {
			ctx = applyPatch(
					type, pack.resources().patched$getResource(type, patchName),
					patchName.toString(), pack, wrapper, audit, trace, context,
					null
					);
		} catch (IOException e) {
			PatchedInternal.LOGGER.warn("Failed to read patch {} from {}:", patchName, from.patched$getName(), e);
		}

		IFileAccess access = null;
		for (String patch : targets.getOrDefault(pack.resources(), List.of())) {
			// We use the IFileAccess instead of grabbing it manually so that it's cached.
			if (access == null)
				access = ctx != null ? ctx.fileAccess() : new PatchedFileAccess(pack.resources());

			final JsonPatch realPatch;

			try {
				realPatch = access.readIncludedPatch(patch);
			} catch (PatchingException e) { // Almost always going to be caused by a missing file.
				PatchedInternal.LOGGER.warn("Failed to read patch {} from {}:\n{}", patch, pack.name(), e.getMessage());
				continue;
			} catch (Exception e) {
				PatchedInternal.LOGGER.warn("Failed to read patch {} from {}:", patch, pack.name(), e);
				continue;
			}

			applyPatch(
					type, realPatch,
					"patches/" + patch + ".json.patch", pack, wrapper, audit, context,
					name.toString()
					);
		}
	}

	private static void traceOverridenPatchesOrFile(
			PatchedPackType type,
			PatchedResourceLocation fileName,
			PatchedResourceLocation patchName,
			PatchedPackResources pack,
			PatchTrace trace,
			List<String> targets) {
		if (pack.patched$hasResource(type, fileName))
			trace.recordFile(pack, true);

		if (pack.patched$hasPatches()) {
			if (pack.patched$hasResource(type, patchName))
				trace.recordPatch(pack, true);

			// We can't actually check targets, because those are already filtered out.
			// Maybe one day.
		}
	}

	private static PatchContext applyPatch(
			PatchedPackType type,
			@Nullable InputStream patchSupplier,
			String patchName,
			Entry pack,
			LazyPatchingWrapper wrapper,
			@Nullable PatchAudit audit,
			PatchTrace trace,
			PatchContext[] context,
			@Nullable String explicitTargetName) {
		if (patchSupplier == null) return null;

		if (explicitTargetName != null)
			trace.recordDynamicPatch(pack.resources(), false);
		else
			trace.recordPatch(pack.resources(), false);

		final String patchJson;

		try (InputStream patchStream = patchSupplier) {
			patchJson = PatchedInternal.readString(patchStream);
		} catch (Exception e) {
			PatchedInternal.LOGGER.warn("Failed to read patch {} from {}:", patchName, pack.name(), e);
			return null;
		}

		final JsonPatch patch;

		try {
			patch = Patches.readPatch(PatchedInternal.GSON, patchJson);
		} catch (Exception e) {
			PatchedInternal.LOGGER.warn("Failed to parse patch {} from {}:", patchName, pack.name(), e);
			return null;
		}

		return applyPatch(type, patch, patchName, pack, wrapper, audit, context, explicitTargetName);
	}

	private static PatchContext applyPatch(
			PatchedPackType type,
			JsonPatch patch,
			String patchName,
			Entry pack,
			LazyPatchingWrapper wrapper,
			@Nullable PatchAudit audit,
			PatchContext[] context,
			String explicitTargetName) {
		try {
			if (audit != null)
				audit.setPatchPath(pack.name());
			if (context[0] == null)
				context[0] = PatchedInternal.BASE_CONTEXT.audit(audit).testEvaluator(new PatchedTestEvaluator(type));

			if (DEBUG)
				PatchedInternal.LOGGER.info("Applying patch {} from {}{}.",
						patchName,
						pack.name(),
						explicitTargetName != null ? " to " + explicitTargetName : "");

			final PatchContext ctx = context[0].fileAccess(new PatchedFileAccess(pack.resources()));

			patch.patch(wrapper.get(), ctx);

			return ctx;
		} catch (BailException e) {
			throw e;
		} catch (PatchingException e) {
			PatchedInternal.LOGGER.warn("Failed to apply patch {} from {}:\n{}", patchName, pack.name(), e.toString());
		} catch (Exception e) {
			PatchedInternal.LOGGER.warn("Failed to apply patch {} from {}:", patchName, pack.name(), e);
		}

		return null;
	}

	/**
	 * Initializes the {@code PatchedMetadata} of the specified pack, if it has not been initialized yet.
	 * @param resources The pack to initialize.
	 */
	public static void maybeInitialize(PatchingPackResources resources) {
		maybeInitialize(new Entry((PatchedPackResources) resources));
	}

	/**
	 * Initializes the {@code PatchedMetadata} of the specified pack, if it has not been initialized yet.
	 * @param entry The pack to initialize.
	 */
	static void maybeInitialize(Entry entry) {
		final PatchedPackResources patching = entry.resources();

		if (!patching.patched$initialized())
			synchronized (patching) {
				if (!patching.patched$initialized()) {
					if (HAS_GROUP_PACKS && patching.patched$isGroupPack()) {
						boolean enabled = false;

						for (PatchedPackResources resources : patching.patched$getChildren())
							enabled |= resources.patched$hasPatches();

						patching.setPatchedMetadata(enabled ? PatchedMetadata.CURRENT_VERSION : PatchedMetadata.DISABLED_METADATA);
					} else {
						PatchedMetadata meta;

						try {
							final InputStream io = entry.resources().patched$getRootResource("pack.mcmeta");
							if (io != null)
								try (InputStream is = io) {
									final String json = PatchedInternal.readString(is);
									final JsonElement elem = JsonParser.parseString(json);

									meta = PatchedMetadata.of(elem, entry.name());
								}
							else
								meta = PatchedMetadata.DISABLED_METADATA;
						} catch (Exception e) {
							PatchedInternal.LOGGER.warn("Failed to read pack.mcmeta in {}:", entry.name(), e);
							meta = PatchedMetadata.DISABLED_METADATA;
						}

						if (!meta.patchingEnabled())
							meta = Objects.requireNonNullElse(
									PatchedPlatform.get().deriveMetadataFromMod(patching),
									meta);

						patching.setPatchedMetadata(meta);
					}

					if (patching.patchedMetadata().patchingEnabled()) {
						if (patching.patchedMetadata().formatVersion() == 0) {
							if (HASPATCHES_WARNING)
								PatchedInternal.LOGGER.warn("Loaded legacy PatchedMetadata from {}. This behavior is deprecated and will be removed in Minecraft 26.1.", entry.name());
							else
								loudDebug("Loaded legacy PatchedMetadata from {}.", entry.name());
						} else
							loudDebug("Loaded PatchedMetadata from {} with format version {}.", entry.name(), patching.patchedMetadata().formatVersion());
					}
				}
			}
	}

	private static void loudDebug(String message, Object... args) {
		if (DEBUG)
			PatchedInternal.LOGGER.info(message, args);
		else
			PatchedInternal.LOGGER.debug(message, args);
	}

	/**
	 * Returns an {@link Iterable} of packs within the given pack.
	 * In most cases, this will only be the given pack.
	 * @param entry The pack.
	 * @param type The pack type.
	 * @param namespace The namespace to look for.
	 * @return The packs containing the specified namespace.
	 */
	private static Iterable<Entry> packsIn(Entry entry, PatchedPackType type, String namespace) {
		if (entry.resources().patched$isGroupPack())
			return Iterables.transform(
					Iterables.filter(
							entry.resources().patched$getFilteredChildren(type, namespace),
							PatchedPackResources::patched$hasPatches),
					Entry::new);

		return List.of(entry);
	}

	/**
	 * <p>Given a pack and a file, tries to find the true source of the file.</p>
	 * <p>
	 * Sometimes a pack may "provide" a file without actually containing it itself.
	 * In particular, mod loaders tend to combine a number of packs together in a way similar to {@code MultiPackResourceManager}.
	 * This is done to condense all the mod resource packs down into one entry in the pack screen (and similar for data packs).
	 * However, we need to know which pack the file actually came from in order to figure out which patches to apply, so that is what this method is for.
	 * </p>
	 * @param from The pack the file is provided by.
	 * @param type The pack type.
	 * @param name The file in question.
	 * @return The true source of the file.
	 */
	private static PatchedPackResources findTrueSource(PatchedPackResources from, PatchedPackType type, PatchedResourceLocation name) {
		if (from.patched$isGroupPack())
			for (PatchedPackResources pack : from.patched$getFilteredChildren(type, name.patched$getNamespace()))
				if (pack.patched$hasResource(type, name))
					return pack;

		return from;
	}
}