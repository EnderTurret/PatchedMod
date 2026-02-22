package net.enderturret.patchedmod.mixin.impl;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.fabric.FallbackResourceManagerAccess;

@Mixin(targets = "net/minecraft/server/packs/resources/FallbackResourceManager$SinglePackResourceThunkSupplier")
public abstract class MixinSinglePackResourceThunkSupplier {

	@Shadow
	@Final
	private FallbackResourceManager this$0;

	@Shadow
	@Final
	private ResourceLocation location;

	@Shadow
	@Final
	private PackResources source;

	@ModifyExpressionValue(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;createResourceGetter("
					+ "Lnet/minecraft/resources/ResourceLocation;"
					+ "Lnet/minecraft/server/packs/PackResources;"
					+ ")Lnet/minecraft/server/packs/resources/Resource$IoSupplier;"),
			method = { "create" })
	private Resource.IoSupplier<InputStream> patched$replaceResourceSingle(Resource.IoSupplier<InputStream> streamSupplier) {
		final FallbackResourceManagerAccess access = (FallbackResourceManagerAccess) this$0;
		return access.patched$chain(streamSupplier, (PatchedResourceLocation) location, (PatchedPackResources) source, true);
	}
}