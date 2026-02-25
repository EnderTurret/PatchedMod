package net.enderturret.patchedmod.mixin.bindings;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.resources.Identifier;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;

@Mixin(Identifier.class)
public abstract class MixinIdentifier implements PatchedResourceLocation {

	@Override
	public String patched$getNamespace() {
		return ((Identifier) (Object) this).getNamespace();
	}

	@Override
	public String patched$getPath() {
		return ((Identifier) (Object) this).getPath();
	}

	@Override
	public PatchedResourceLocation patched$withPath(String path) {
		return (PatchedResourceLocation) (Object) ((Identifier) (Object) this).withPath(path);
	}
}