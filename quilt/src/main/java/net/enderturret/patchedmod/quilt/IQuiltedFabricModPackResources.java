package net.enderturret.patchedmod.quilt;

import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * An interface that's slapped onto {@code ModResourcePack} so we can avoid a hard runtime dependency on Fabric API.
 * @author EnderTurret
 */
@SuppressWarnings("deprecation")
public interface IQuiltedFabricModPackResources {

	public ModMetadata patched$getFabricModMetadata();
}