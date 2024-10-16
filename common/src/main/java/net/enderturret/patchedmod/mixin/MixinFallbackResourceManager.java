package net.enderturret.patchedmod.mixin;

import java.io.InputStream;
import java.util.Objects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;

import net.enderturret.patchedmod.util.MixinCallbacks;

/**
 * <p>This mixin implements the functionality for actually patching resources.</p>
 * <p>This is done by wrapping the {@link IoSupplier}
 * returned by {@link FallbackResourceManager#wrapForDebug(ResourceLocation, PackResources, IoSupplier)} with
 * {@link MixinCallbacks#chain(IoSupplier, FallbackResourceManager, PackType, ResourceLocation, PackResources) MixinCallbacks.chain(IoSupplier, FallbackResourceManager, PackType, ResourceLocation, PackResources)}.</p>
 * @author EnderTurret
 */
@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager {

	@Shadow
	@Final
	private PackType type;

	private static final ThreadLocal<FallbackResourceManager> THIS = new ThreadLocal<>();

	@Inject(at = @At(value = "HEAD"), method = { "getResource", "listResourceStacks", "listResources" })
	private void patched$captureThis(CallbackInfoReturnable<?> cir) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		THIS.set(self);
	}

	@Inject(at = @At(value = "RETURN"), method = { "getResource", "listResourceStacks", "listResources" })
	private void patched$releaseThis(CallbackInfoReturnable<?> cir) {
		THIS.set(null);
	}

	@Inject(at = @At(value = "RETURN"), method = "wrapForDebug", cancellable = true)
	private static void patched$replaceResource(ResourceLocation location, PackResources pack, IoSupplier<InputStream> old, CallbackInfoReturnable<IoSupplier<InputStream>> cir) {
		final var sup = cir.getReturnValue();
		final FallbackResourceManager self = Objects.requireNonNull(THIS.get(), "Captured this shouldn't be null! Did a mixin fail?");
		cir.setReturnValue(MixinCallbacks.chain(sup, self, type, location, pack));
	}
}