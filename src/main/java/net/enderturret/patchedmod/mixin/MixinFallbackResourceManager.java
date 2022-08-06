package net.enderturret.patchedmod.mixin;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;

import net.enderturret.patchedmod.util.MixinCallbacks;

/**
 * <p>This mixin implements the functionality for actually patching resources.</p>
 * <p>This is done by redirecting the {@code new SimpleResource(...)} invocations to
 * {@link MixinCallbacks#chain(Resource.IoSupplier, FallbackResourceManager, ResourceLocation, PackResources)}.</p>
 * @author EnderTurret
 */
@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager {

	@Inject(at = @At(value = "RETURN"), method = "createResourceGetter", cancellable = true)
	private void replaceResource(ResourceLocation location, PackResources pack, CallbackInfoReturnable<Resource.IoSupplier<InputStream>> cir) {
		final var sup = cir.getReturnValue();
		cir.setReturnValue(MixinCallbacks.chain(sup, (FallbackResourceManager) (Object) this, location, pack));
	}
}