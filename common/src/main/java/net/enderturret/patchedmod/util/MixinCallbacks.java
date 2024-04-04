package net.enderturret.patchedmod.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.FallbackResourceManager.PackEntry;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.Patches;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.mixin.FallbackResourceManagerAccess;

/**
 * <p>Handles callbacks from mixins -- what you would expect from the name.</p>
 * <p>Specifically, this handles the overall management of patching files and setting up packs for patching.</p>
 * @author EnderTurret
 */
@Internal
public class MixinCallbacks {

	@Internal
	public static final boolean DEBUG = Boolean.getBoolean("patched.debug");

	private static final boolean HASPATCHES_WARNING = true;

	private static boolean logExceptions = true;

	/**
	 * "Chains" the given {@code IoSupplier}, returning an {@code IoSupplier} that patches the data returned by it.
	 * @param delegate The delegate {@code IoSupplier}.
	 * @param manager The resource manager that the data is from.
	 * @param name The location of the data.
	 * @param origin The resource or data pack that the data originated from.
	 * @return The new {@code IoSupplier}.
	 */
	@Internal
	public static IoSupplier<InputStream> chain(IoSupplier<InputStream> delegate, FallbackResourceManager manager, ResourceLocation name, PackResources origin) {
		return () -> new PatchingInputStream(delegate, (stream, audit) -> {
			if (!(manager instanceof FallbackResourceManagerAccess access))
				return stream; // Don't attempt patching if our mixin hasn't applied for some reason.

			return patch(manager, origin, access.getType(), name, stream, audit);
		});
	}

	/**
	 * Patches the data from the given stream, returning the patched data as a stream.
	 * @param manager The resource manager that the data is from.
	 * @param from The resource or data pack that the data originated from.
	 * @param type The type of pack this data is from.
	 * @param name The location of the data.
	 * @param stream The data stream.
	 * @param audit The audit to record changes made by the patches.
	 * @return A new stream containing the patched data.
	 */
	private static InputStream patch(FallbackResourceManager manager, PackResources from, PackType type, ResourceLocation name, InputStream stream, @Nullable PatchAudit audit) {
		if (stream == null || !PatchUtil.isPatchable(name)) return stream;

		final LazyPatchingWrapper wrapper = new LazyPatchingWrapper(stream);

		try {
			patch(manager, from, type, name, wrapper, audit);
		} catch (BailException e) {
			// Let the future data consumer handle these.
		} catch (Exception e) {
			if (logExceptions) {
				Patched.platform().logger().error("An exception occurred while attempting to patch {}. Further exceptions will not be reported.", name, e);
				logExceptions = false;
			}
		}

		return wrapper.getOrCreateStream();
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
	private static boolean patch(FallbackResourceManager manager, PackResources from, PackType type, ResourceLocation name, LazyPatchingWrapper wrapper, @Nullable PatchAudit audit) {
		final ResourceLocation patchName = new ResourceLocation(name.getNamespace(), name.getPath() + ".patch");

		PatchContext context = null;

		from = findTrueSource(from, type, name);

		for (int i = manager.fallbacks.size() - 1; i >= 0; i--) {
			final PackEntry packEntry = manager.fallbacks.get(i);
			if (packEntry.resources() == null) continue;
			final Entry entry = new Entry(packEntry);

			if (hasPatches(entry.resources))
				for (Entry pack : packsIn(entry, type, patchName)) {
					final String patchJson;

					try (InputStream patchStream = pack.resources().getResource(type, patchName).get()) {
						patchJson = PatchUtil.readString(patchStream);
					} catch (Exception e) {
						Patched.platform().logger().warn("Failed to read patch {} from {}:", patchName, pack.name(), e);
						continue;
					}

					final JsonPatch patch;

					try {
						patch = Patches.readPatch(PatchUtil.GSON, patchJson);
					} catch (Exception e) {
						Patched.platform().logger().warn("Failed to parse patch {} from {}:", patchName, pack.name(), e);
						continue;
					}

					try {
						if (audit != null)
							audit.setPatchPath(pack.name());
						if (context == null)
							context = PatchUtil.CONTEXT.audit(audit);

						Patched.platform().logger().atLevel(DEBUG ? Level.INFO : Level.DEBUG).log("Applying patch {} from {}.", patchName, pack.name());

						patch.patch(wrapper.get(), context);
					} catch (BailException e) {
						throw e;
					} catch (PatchingException e) {
						Patched.platform().logger().warn("Failed to apply patch {} from {}:\n{}", patchName, pack.name(), e.toString());
					} catch (Exception e) {
						Patched.platform().logger().warn("Failed to apply patch {} from {}:", patchName, pack.name(), e);
					}

					if (pack.resources() == from)
						break;
				}
		}

		return context != null;
	}

	/**
	 * Determines whether the given pack has patches enabled.
	 * If necessary, the pack may be {@linkplain IPatchingPackResources#initialized() initialized}.
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
	static void maybeInitialize(PackResources resources) {
		maybeInitialize(new Entry(resources));
	}

	/**
	 * Initializes the {@code PatchedMetadata} of the specified pack, if it has not been initialized yet.
	 * @param entry The pack to initialize.
	 */
	static void maybeInitialize(Entry entry) {
		if (!(entry.resources() instanceof IPatchingPackResources patching))
			return;

		if (!patching.initialized())
			synchronized (patching) {
				if (!patching.initialized()) {
					if (Patched.platform().isGroup(entry.resources())) {
						boolean enabled = false;

						for (PackResources resources : Patched.platform().getChildren(entry.resources()))
							enabled |= hasPatches(resources);

						patching.setPatchedMetadata(enabled ? PatchedMetadata.CURRENT_VERSION : PatchedMetadata.DISABLED_METADATA);
					} else {
						final IoSupplier<InputStream> io = entry.resources().getRootResource("pack.mcmeta");
						if (io != null)
							try (InputStream is = io.get()) {
								final String json = PatchUtil.readString(is);
								final JsonElement elem = JsonParser.parseString(json);

								patching.setPatchedMetadata(PatchedMetadata.of(elem, entry.name));
							} catch (Exception e) {
								Patched.platform().logger().warn("Failed to read pack.mcmeta in {}:", entry.name(), e);
								patching.setPatchedMetadata(PatchedMetadata.DISABLED_METADATA);
							}
						else
							patching.setPatchedMetadata(PatchedMetadata.DISABLED_METADATA);
					}

					if (patching.patchedMetadata().patchingEnabled()) {
						if (patching.patchedMetadata().formatVersion() == 0) {
							if (HASPATCHES_WARNING)
								Patched.platform().logger().warn("Loaded legacy PatchedMetadata from {}. This behavior is deprecated and will be removed in a future release.", entry.name());
							else
								Patched.platform().logger().atLevel(DEBUG ? Level.INFO : Level.DEBUG).log("Loaded legacy PatchedMetadata from {}.", entry.name());
						} else
							Patched.platform().logger().atLevel(DEBUG ? Level.INFO : Level.DEBUG).log("Loaded PatchedMetadata from {} with format version {}.", entry.name(), patching.patchedMetadata().formatVersion());
					}
				}
			}
	}

	/**
	 * Returns an {@link Iterable} of packs containing the specified patch within the given pack.
	 * In most cases, this will only be the given pack.
	 * @param entry The pack.
	 * @param type The pack type.
	 * @param patchName The patch to look for.
	 * @return The packs containing the specified patch.
	 */
	private static Iterable<Entry> packsIn(Entry entry, PackType type, ResourceLocation patchName) {
		if (Patched.platform().isGroup(entry.resources()))
			return Iterables.transform(
					Iterables.filter(Patched.platform().getFilteredChildren(entry.resources(), type, patchName),
							pack -> hasPatches(pack) && pack.getResource(type, patchName) != null),
					Entry::new);
		else if (hasPatches(entry.resources) && entry.resources().getResource(type, patchName) != null)
			return List.of(entry);

		return List.of();
	}

	/**
	 * <p>Given a pack and a file, tries to find the true source of the file.</p>
	 * <p>
	 * Sometimes a pack may "provide" a file without actually containing it itself.
	 * In particular, mod loaders tend to combine a number of packs together in a way similar to {@link MultiPackResourceManager}.
	 * This is done to condense all the mod resource packs down into one entry in the pack screen (and similar for data packs).
	 * However, we need to know which pack the file actually came from in order to figure out which patches to apply, so that is what this method is for.
	 * </p>
	 * @param from The pack the file is provided by.
	 * @param type The pack type.
	 * @param name The file in question.
	 * @return The true source of the file.
	 */
	private static PackResources findTrueSource(PackResources from, PackType type, ResourceLocation name) {
		if (Patched.platform().isGroup(from))
			for (PackResources pack : Patched.platform().getFilteredChildren(from, type, name))
				if (pack.getResource(type, name) != null)
					return pack;

		return from;
	}

	/**
	 * An alternative to ATing {@link PackEntry}'s constructor public.
	 * @author EnderTurret
	 * @param name The name of the pack.
	 * @param resources The pack itself.
	 */
	static record Entry(String name, PackResources resources) {

		Entry {}

		Entry(PackEntry packEntry) {
			this(Objects.requireNonNull(packEntry.resources(), "packEntry.resources()"));
		}

		Entry(PackResources resources) {
			this(Patched.platform().getName(Objects.requireNonNull(resources, "resources")), resources);
		}
	}

	/**
	 * A class that wraps an {@link InputStream} in such a way that we can avoid reading from it if no patching is performed.
	 * @author EnderTurret
	 */
	private static class LazyPatchingWrapper {

		private InputStream stream;
		private byte[] oldBytes;
		private JsonDocument doc;

		public LazyPatchingWrapper(InputStream stream) {
			this.stream = stream;
		}

		public InputStream getOrCreateStream() {
			if (oldBytes == null) return Objects.requireNonNull(stream);

			if (doc != null)
				oldBytes = PatchUtil.GSON.toJson(doc.getRoot()).getBytes(StandardCharsets.UTF_8);

			return new ByteArrayInputStream(oldBytes);
		}

		public JsonDocument get() {
			if (doc == null)
				doc = new JsonDocument(read());

			return doc;
		}

		private JsonElement read() {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {
				stream.transferTo(baos);
				stream.close();
				stream = null;
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to transfer data to byte array", e);
			}

			oldBytes = baos.toByteArray();

			String json = new String(oldBytes, StandardCharsets.UTF_8);

			try {
				return JsonParser.parseString(json);
			} catch (Exception e) {
				throw new BailException(e);
			}
		}
	}

	/**
	 * An exception thrown to signal that we should really just bail out and let someone else handle this mess.
	 * @author EnderTurret
	 */
	private static class BailException extends RuntimeException {

		public BailException() {}
		public BailException(Throwable cause) { super(cause); }
	}
}