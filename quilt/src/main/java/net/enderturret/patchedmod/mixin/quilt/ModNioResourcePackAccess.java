package net.enderturret.patchedmod.mixin.quilt;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.resource.loader.impl.ModNioPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModNioPack.class)
public interface ModNioResourcePackAccess {

	@Accessor(remap = false)
	public ModMetadata getModInfo();
}