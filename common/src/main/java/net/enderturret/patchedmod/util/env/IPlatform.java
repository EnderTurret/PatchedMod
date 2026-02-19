package net.enderturret.patchedmod.util.env;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.common.env.IPatchingPackResources;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

/**
 * An abstraction over the different loaders Patched supports.
 * @author EnderTurret
 */
@Internal
public interface IPlatform {

	/**
	 * Returns Patched's {@code Logger} instance.
	 * @return Patched's {@code Logger} instance.
	 */
	public default Logger logger() { return PatchedInternal.LOGGER; }

	/**
	 * Returns whether or not Patched is running on the (physical) client.
	 * @return {@code true} if Patched is running on the client.
	 */
	public boolean isPhysicalClient();

	/**
	 * Returns whether or not a mod with the specified mod ID is loaded.
	 * @param modId The mod ID to check.
	 * @return {@code true} if the mod is loaded.
	 */
	public boolean isModLoaded(String modId);

	/**
	 * Returns whether or not a mod with the specified mod ID is loaded <i>and</i> is at least the specified version.
	 * @param modId The mod ID to check.
	 * @param version The minimum version of the mod to require.
	 * @return {@code true} if the mod is loaded and is <i>at least</i> the specified version.
	 */
	public boolean isModLoaded(String modId, String version);

	/**
	 * If the specified pack belongs to a mod, and no {@code pack.mcmeta} exists for it,
	 * this method tries to derive a {@link PatchedMetadata} from the mod's {@code mods.toml} / {@code neoforge.mods.toml} / {@code fabric.mods.json} / {@code quilt.mods.json}.
	 * @param pack The pack in question.
	 * @return A derived {@link PatchedMetadata}, or {@code null} if one could not be derived.
	 */
	@Nullable
	public default PatchedMetadata deriveMetadataFromMod(IPatchingPackResources pack) {
		return null;
	}

	/**
	 * Returns a {@code Stream} over all packs in the given resource manager, expanding {@linkplain IPatchingPackResources#patched$getChildren() group packs} as necessary.
	 * @param manager The resource manager to query the packs of.
	 * @return The stream.
	 */
	public default Stream<IPatchingPackResources> getExpandedPacks(ResourceManager manager) {
		return manager.listPacks()
				.map(p -> (IPatchingPackResources) p)
				.flatMap(p -> p.patched$isGroupPack() ? p.patched$getChildren().stream() : Stream.of(p));
	}

	/**
	 * Returns a {@code Stream} over all patching-enabled packs in the given resource manager, expanding {@linkplain IPatchingPackResources#patched$getChildren() group packs} as necessary.
	 * This functions like {@link #getExpandedPacks(ResourceManager)}, but additionally filtering out non-patching packs.
	 * @param manager The resource manager to query the packs of.
	 * @return The stream.
	 */
	public default Stream<IPatchingPackResources> getPatchingPacks(ResourceManager manager) {
		return manager.listPacks().map(p -> (IPatchingPackResources) p).filter(this::hasPatches);
	}

	/**
	 * A convenience method to check whether or not the specified pack has patching enabled.
	 * @param pack The pack in question.
	 * @return {@code true} if the pack has patching enabled.
	 */
	public default boolean hasPatches(IPatchingPackResources pack) {
		return pack.patchedMetadata().patchingEnabled();
	}
}