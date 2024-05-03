package net.enderturret.patchedmod.fabric;

import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * An interface that's slapped onto {@code ModResourcePack} so we can avoid a hard runtime dependency on Fabric API.
 * @author EnderTurret
 */
public interface IFabricModPackResources {

	public ModMetadata patched$getFabricModMetadata();
}