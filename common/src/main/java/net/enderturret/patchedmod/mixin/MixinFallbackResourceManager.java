package net.enderturret.patchedmod.mixin;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;

import net.enderturret.patchedmod.util.MixinCallbacks;

/**
 * <p>This mixin implements the functionality for actually patching resources.</p>
 * <p>This is done by wrapping the {@link InputStream}
 * returned by {@link FallbackResourceManager#getWrappedResource(ResourceLocation, PackResources)} with
 * {@link MixinCallbacks#chain(InputStream, FallbackResourceManager, ResourceLocation, PackResources) MixinCallbacks.chain(InputStream, FallbackResourceManager, ResourceLocation, PackResources)}.</p>
 * @author EnderTurret
 */
@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager {

	@Inject(at = @At(value = "RETURN"), method = "getWrappedResource", cancellable = true)
	private void patched$replaceResource(ResourceLocation location, PackResources pack, CallbackInfoReturnable<InputStream> cir) {
		final var sup = cir.getReturnValue();
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		cir.setReturnValue(MixinCallbacks.chain(sup, self, location, pack));
	}
}