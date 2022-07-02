package net.enderturret.patchedmod.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.ResourcePackFileNotFoundException;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;

import net.enderturret.patched.Patches;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patchedmod.Patched;

/**
 * <p>Handles callbacks from mixins -- what you would expect given the name.</p>
 * <p>Specifically, this handles actually patching things.</p>
 * @see #loadResource(FallbackResourceManager, String, ResourceLocation, InputStream, InputStream)
 * @author EnderTurret
 */
public class MixinCallbacks {

	/**
	 * Handles patching a resource, if possible.
	 * @param manager The resource manager that this resource is from.
	 * @param sourceName The resource/data pack that the resource is from.
	 * @param name The location of the resource.
	 * @param resource The contents of the resource itself.
	 * @param metadata The contents of the resource's .mcmeta file, if present. {@code null} means that no such .mcmeta file exists.
	 * @return The patched resource.
	 */
	@SuppressWarnings("resource")
	public static SimpleResource loadResource(FallbackResourceManager manager, String sourceName, ResourceLocation name, InputStream resource, @Nullable InputStream metadata) {
		if (Patched.canBePatched(name)) {
			// You might be wondering: why can't this just go in the mixin using @Shadow?
			// And the answer is: because refmaps refuse to work for a variety of reasons.
			final PackType type = ReflectionUtil.getType(manager);

			resource = patch(manager, type, name, resource);

			if (metadata != null)
				metadata = patch(manager, type, new ResourceLocation(name.getNamespace(), name.getPath() + ".mcmeta"), metadata);
		}

		return new SimpleResource(sourceName, name, resource, metadata);
	}

	/**
	 * Patches the data from the given stream, returning the patched data as a stream.
	 * @param manager The resource manager that the data is from.
	 * @param type The type of pack this data is from.
	 * @param name The location of the data.
	 * @param stream The data stream.
	 * @return A new stream containing the patched data.
	 */
	private static InputStream patch(FallbackResourceManager manager, PackType type, ResourceLocation name, InputStream stream) {
		if (stream == null) return stream;

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			stream.transferTo(baos);
			stream.close();
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to transfer data to byte array", e);
		}

		byte[] bytes = baos.toByteArray();

		String json = new String(bytes, StandardCharsets.UTF_8);

		try {
			final JsonElement elem = JsonParser.parseString(json);

			patch(manager, type, name, elem);

			json = PatchUtil.GSON.toJson(elem);
			bytes = json.getBytes(StandardCharsets.UTF_8);
		} catch (JsonParseException e) {
			// Let the future data consumer handle this.
		}

		return new ByteArrayInputStream(bytes);
	}

	/**
	 * <p>Patches the given Json data using patches from all of the packs with the given pack type.</p>
	 * <p>The Json data is manipulated directly, so don't pass in anything you don't want modified.</p>
	 * @param manager The resource manager that the Json data is from.
	 * @param type The type of pack this Json data is from.
	 * @param name The location of the Json data.
	 * @param elem The Json data to patch.
	 */
	@SuppressWarnings("resource")
	private static void patch(FallbackResourceManager manager, PackType type, ResourceLocation name, JsonElement elem) {
		final ResourceLocation patchName = new ResourceLocation(name.getNamespace(), name.getPath() + ".patch");

		for (int i = manager.fallbacks.size() - 1; i >= 0; i--) {
			final PackResources pack = manager.fallbacks.get(i);

			if (hasPatches(pack) && pack.hasResource(type, patchName)) {
				final String patchJson;

				try (InputStream patchStream = pack.getResource(type, patchName)) {
					patchJson = PatchUtil.readString(patchStream);
				} catch (IOException e) {
					Patched.LOGGER.warn("Failed to read patch {} from {}:", patchName, pack.getName(), e);
					continue;
				}

				final JsonPatch patch;

				try {
					patch = Patches.readPatch(PatchUtil.GSON, patchJson);
				} catch (JsonParseException e) {
					Patched.LOGGER.warn("Failed to parse patch {} from {}:", patchName, pack.getName(), e);
					continue;
				}

				try {
					Patched.LOGGER.info("Applying patch {} from {}.", patchName, pack.getName());
					patch.patch(elem, PatchUtil.CONTEXT);
				} catch (PatchingException e) {
					Patched.LOGGER.warn("Failed to apply patch {} from {}:\n{}", patchName, pack.getName(), e.toString());
				} catch (Exception e) {
					Patched.LOGGER.warn("Failed to apply patch {} from {}:", patchName, pack.getName(), e);
				}
			}
		}
	}

	/**
	 * Determines whether the given pack has patches.
	 * If necessary, the pack may be {@linkplain IPatchingPackResources#initialized() initialized}.
	 * @param resources
	 * @return
	 */
	private static boolean hasPatches(PackResources resources) {
		if (!(resources instanceof IPatchingPackResources patching))
			return false;

		if (!patching.initialized())
			synchronized (patching) {
				if (!patching.initialized()) {
					try (InputStream is = resources.getRootResource("pack.mcmeta")) {
						final String json = PatchUtil.readString(is);
						final JsonElement elem = JsonParser.parseString(json);

						if (elem instanceof JsonObject o
								&& o.get("pack") instanceof JsonObject pack
								&& pack.get("patched:has_patches") instanceof JsonPrimitive prim
								&& prim.isBoolean())
							patching.setHasPatches(prim.getAsBoolean());

						else patching.setHasPatches(false);
					} catch (ResourcePackFileNotFoundException e) {
						patching.setHasPatches(false);
					} catch (Exception e) {
						Patched.LOGGER.error("Failed to read pack.mcmeta in {}:", resources.getName(), e);
						patching.setHasPatches(false);
					}

					if (patching.hasPatches())
						Patched.LOGGER.info("Enabled patching for {} ({}).", resources.getName(), resources);
				}
			}

		return patching.hasPatches();
	}
}