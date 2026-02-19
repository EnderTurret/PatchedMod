package net.enderturret.patchedmod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.CloseableResourceManager;

import net.enderturret.patchedmod.common.internal.PatchTargetManager;
import net.enderturret.patchedmod.common.internal.flow.DynamicPatches;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Handles setting up the data pack {@link PatchTargetManager}.
 * @author EnderTurret
 */
@Mixin(WorldLoader.PackConfig.class)
public abstract class MixinPackConfig {

	@Inject(at = @At("RETURN"), method = "createResourceManager")
	private void patched$setupServerPatchTargetManager(CallbackInfoReturnable<Pair<?, CloseableResourceManager>> cir) {
		DynamicPatches.setupTargetManager(PatchedPackType.SERVER_DATA, (List) cir.getReturnValue().getSecond().listPacks().toList());
	}
}