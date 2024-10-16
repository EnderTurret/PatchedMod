package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;

import net.enderturret.patchedmod.util.MixinCallbacks;

@Mixin(WorldLoader.PackConfig.class)
public abstract class MixinPackConfig {

	@Inject(at = @At("RETURN"), method = "createResourceManager")
	private void patched$setupServerPatchTargetManager(CallbackInfoReturnable<Pair<?, CloseableResourceManager>> cir) {
		MixinCallbacks.setupTargetManager(PackType.SERVER_DATA, cir.getReturnValue().getSecond().listPacks().toList());
	}
}