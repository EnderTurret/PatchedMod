package net.enderturret.patchedmod.util.env;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
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
	public default PatchedMetadata deriveMetadataFromMod(PatchedPackResources pack) {
		return null;
	}
}