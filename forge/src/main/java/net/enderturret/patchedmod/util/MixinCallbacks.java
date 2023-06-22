package net.enderturret.patchedmod.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Iterables;
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
import net.minecraft.server.packs.resources.FallbackResourceManager.PackEntry;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;

import net.minecraftforge.resource.DelegatingPackResources;

import net.enderturret.patched.Patches;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.mixin.forge.DelegatingPackResourcesAccess;

/**
 * <p>Handles callbacks from mixins -- what you would expect from the name.</p>
 * <p>Specifically, this handles actually patching things.</p>
 * @author EnderTurret
 */
@Internal
public class MixinCallbacks {

	@Internal
	private static final boolean DEBUG = Boolean.getBoolean("patched.debug");

	/**
	 * "Chains" the given {@code IoSupplier}, returning an {@code IoSupplier} that patches the data returned by it.
	 * @param delegate The delegate {@code IoSupplier}.
	 * @param manager The resource manager that the data is from.
	 * @param name The location of the data.
	 * @param origin The resource or data pack that the data originated from.
	 * @return The new {@code IoSupplier}.
	 */
	@Internal
	public static Resource.IoSupplier<InputStream> chain(Resource.IoSupplier<InputStream> delegate, FallbackResourceManager manager, ResourceLocation name, PackResources origin) {
		return () -> new PatchingInputStream(delegate, (stream, audit) -> patch(manager, origin, manager.type, name, stream, audit));
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

			patch(manager, from, type, name, elem, audit);

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
	 * @param from The resource or data pack that the data originated from.
	 * @param type The type of pack this Json data is from.
	 * @param name The location of the Json data.
	 * @param elem The Json data to patch.
	 * @param audit The audit to record changes made by the patches.
	 */
	@SuppressWarnings("resource")
	private static void patch(FallbackResourceManager manager, PackResources from, PackType type, ResourceLocation name, JsonElement elem, @Nullable PatchAudit audit) {
		final ResourceLocation patchName = new ResourceLocation(name.getNamespace(), name.getPath() + ".patch");

		PatchContext context = null;

		from = findTrueSource(from, type, name);

		for (int i = manager.fallbacks.size() - 1; i >= 0; i--) {
			final PackEntry packEntry = manager.fallbacks.get(i);
			if (hasPatches(packEntry.name(), packEntry.resources()))
				for (PackEntry pack : packsIn(packEntry, type, patchName)) {
					final String patchJson;

					try (InputStream patchStream = pack.resources().getResource(type, patchName)) {
						patchJson = PatchUtil.readString(patchStream);
					} catch (IOException e) {
						Patched.LOGGER.warn("Failed to read patch {} from {}:", patchName, pack.name(), e);
						continue;
					}

					final JsonPatch patch;

					try {
						patch = Patches.readPatch(PatchUtil.GSON, patchJson);
					} catch (JsonParseException e) {
						Patched.LOGGER.warn("Failed to parse patch {} from {}:", patchName, pack.name(), e);
						continue;
					}

					try {
						if (audit != null)
							audit.setPatchPath(pack.name());

						Patched.LOGGER.debug("Applying patch {} from {}.", patchName, pack.name());
						patch.patch(elem, context == null ? context = PatchUtil.CONTEXT.audit(audit) : context);
					} catch (PatchingException e) {
						Patched.LOGGER.warn("Failed to apply patch {} from {}:\n{}", patchName, pack.name(), e.toString());
					} catch (Exception e) {
						Patched.LOGGER.warn("Failed to apply patch {} from {}:", patchName, pack.name(), e);
					}

					if (pack.resources() == from)
						break;
				}
		}
	}

	/**
	 * Determines whether the given pack has patches enabled.
	 * If necessary, the pack may be {@linkplain IPatchingPackResources#initialized() initialized}.
	 * @param packName The name of the pack.
	 * @param packResources The pack to check.
	 * @return {@code true} if the pack has patches enabled.
	 */
	@SuppressWarnings("resource")
	private static boolean hasPatches(String packName, PackResources packResources) {
		if (!(packResources instanceof IPatchingPackResources patching))
			return false;

		if (!patching.initialized())
			synchronized (patching) {
				if (!patching.initialized()) {
					if (patching instanceof DelegatingPackResourcesAccess dpra) {
						boolean enabled = false;
						for (PackResources resources : dpra.getDelegates())
							enabled |= hasPatches(resources.getName(), resources);
						patching.setHasPatches(enabled);
					} else
						try (InputStream is = packResources.getRootResource("pack.mcmeta")) {
							final String json = PatchUtil.readString(is);
							final JsonElement elem = JsonParser.parseString(json);

							if (elem instanceof JsonObject o
									&& o.get("pack") instanceof JsonObject packObj
									&& packObj.get("patched:has_patches") instanceof JsonPrimitive prim
									&& prim.isBoolean())
								patching.setHasPatches(prim.getAsBoolean());

							else patching.setHasPatches(false);
						} catch (ResourcePackFileNotFoundException e) {
							patching.setHasPatches(false);
						} catch (Exception e) {
							Patched.LOGGER.error("Failed to read pack.mcmeta in {}:", packName, e);
							patching.setHasPatches(false);
						}

					if (patching.hasPatches())
						Patched.LOGGER.debug("Enabled patching for {}.", packName);

					if (DEBUG)
						Patched.LOGGER.debug("{} patches state: {}", packName, patching.hasPatches());
				}
			}

		return patching.hasPatches();
	}

	/**
	 * Returns an {@link Iterable} of packs containing the specified patch within the given pack.
	 * In most cases, this will only be the given pack.
	 * @param entry The pack.
	 * @param type The pack type.
	 * @param patchName The patch to look for.
	 * @return The packs containing the specified patch.
	 */
	private static Iterable<PackEntry> packsIn(PackEntry entry, PackType type, ResourceLocation patchName) {
		if (entry.resources() instanceof DelegatingPackResourcesAccess dpra) {
			return Iterables.transform(
					Iterables.filter(dpra.callGetCandidatePacks(type, patchName),
							pack -> hasPatches(pack.getName(), pack) && pack.hasResource(type, patchName)),
					pack -> new PackEntry(pack.getName(), pack, null));
		} else if (hasPatches(entry.name(), entry.resources()) && entry.resources().hasResource(type, patchName))
			return List.of(entry);

		return List.of();
	}

	/**
	 * <p>Given a pack and a file, tries to find the true source of the file.</p>
	 * <p>
	 * Sometimes a pack may "provide" a file without actually containing it itself.
	 * In particular, {@link DelegatingPackResources} combines a number of packs together, like {@link MultiPackResourceManager}.
	 * We need to know which pack the file actually came from in order to figure out which patches to apply, so that is what this method is for.
	 * </p>
	 * @param from The pack the file is provided by.
	 * @param type The pack type.
	 * @param name The file in question.
	 * @return The true source of the file.
	 */
	private static PackResources findTrueSource(PackResources from, PackType type, ResourceLocation name) {
		if (from instanceof DelegatingPackResourcesAccess dpra)
			for (PackResources pack : dpra.callGetCandidatePacks(type, name))
				if (pack.hasResource(type, name))
					return pack;

		return from;
	}
}