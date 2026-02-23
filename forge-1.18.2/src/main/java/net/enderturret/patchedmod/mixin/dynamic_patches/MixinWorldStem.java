package net.enderturret.patchedmod.mixin.dynamic_patches;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

import net.enderturret.patchedmod.common.internal.PatchTargetManager;
import net.enderturret.patchedmod.common.internal.flow.DynamicPatches;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Handles setting up the data pack {@link PatchTargetManager}.
 * @author EnderTurret
 */
@Mixin(WorldStem.class)
public abstract class MixinWorldStem {

	@WrapOperation(
			at = @At(
					value = "NEW",
					target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;"
					),
			method = "load"
	)
	private MultiPackResourceManager patched$setupServerPatchTargetManager(
			PackType type, List<PackResources> packs, Operation<MultiPackResourceManager> original) {
		DynamicPatches.setupTargetManager(type == PackType.CLIENT_RESOURCES ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA, (List) packs);
		return original.call(type, packs);
	}
}