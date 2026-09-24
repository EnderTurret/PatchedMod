package net.enderturret.patchedmod.fabric;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * An interface that's slapped onto {@code ModResourcePack} so we can avoid a hard runtime dependency on Fabric API.
 * @author EnderTurret
 */
@Internal
public interface IFabricModPackResources {

	/**
	 * Provides access to the {@code PackResources}'s {@code ModMetadata}.
	 * @return The mod metadata.
	 */
	public ModMetadata patched$getFabricModMetadata();
}