package net.enderturret.patchedmod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

import net.enderturret.patchedmod.internal.PatchTargetManager;
import net.enderturret.patchedmod.internal.flow.DynamicPatches;

/**
 * Handles setting up the data pack {@link PatchTargetManager} when running /reload.
 * @author EnderTurret
 */
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

	@WrapOperation(
			at = @At(
					value = "NEW",
					target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;"
					),
			method = "*"
	)
	private MultiPackResourceManager patched$setupServerPatchTargetManager(
			PackType type, List<PackResources> packs, Operation<MultiPackResourceManager> original) {
		DynamicPatches.setupTargetManager(type, packs);
		return original.call(type, packs);
	}
}