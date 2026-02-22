package net.enderturret.patchedmod.mixin.fabric.api;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.loader.api.metadata.ModMetadata;

import net.enderturret.patchedmod.fabric.IFabricModPackResources;

@Mixin(ModResourcePack.class)
public interface MixinModResourcePack extends IFabricModPackResources {

	@Override
	public default ModMetadata patched$getFabricModMetadata() {
		return ((ModResourcePack) this).getFabricModMetadata();
	}
}