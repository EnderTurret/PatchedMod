package net.enderturret.patchedmod.mixin.bindings;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;

/**
 * Implements common bindings for {@code ResourceLocation}.
 * @author EnderTurret
 */
@Mixin(ResourceLocation.class)
public abstract class MixinResourceLocation implements PatchedResourceLocation {

	@Override
	public String patched$getNamespace() {
		return ((ResourceLocation) (Object) this).getNamespace();
	}

	@Override
	public String patched$getPath() {
		return ((ResourceLocation) (Object) this).getPath();
	}

	@SuppressWarnings("removal")
	@Override
	public PatchedResourceLocation patched$withPath(String path) {
		return (PatchedResourceLocation) new ResourceLocation(patched$getNamespace(), path);
	}
}