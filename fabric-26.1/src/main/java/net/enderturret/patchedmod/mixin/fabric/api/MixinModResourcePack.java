package net.enderturret.patchedmod.mixin.fabric.api;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.fabricmc.loader.api.metadata.ModMetadata;

import net.enderturret.patchedmod.fabric.IFabricModPackResources;

/**
 * Implements {@link IFabricModPackResources} on {@link ModPackResources}.
 * @author EnderTurret
 */
@Mixin(ModPackResources.class)
public interface MixinModResourcePack extends IFabricModPackResources {

	@Override
	public default ModMetadata patched$getFabricModMetadata() {
		return ((ModPackResources) this).getFabricModMetadata();
	}
}