package net.enderturret.patchedmod.mixin.quilt.api;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModNioResourcePack.class)
public interface ModNioResourcePackAccess {

	@Accessor(remap = false)
	public ModMetadata getModInfo();
}