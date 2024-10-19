package net.enderturret.patchedmod.mixin.quilt.api;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.loader.api.metadata.ModMetadata;

import net.enderturret.patchedmod.quilt.IQuiltedFabricModPackResources;

@Mixin(ModResourcePack.class)
@SuppressWarnings("deprecation")
public interface MixinModResourcePack extends IQuiltedFabricModPackResources {

	@Override
	public default ModMetadata patched$getFabricModMetadata() {
		return ((ModResourcePack) this).getFabricModMetadata();
	}
}