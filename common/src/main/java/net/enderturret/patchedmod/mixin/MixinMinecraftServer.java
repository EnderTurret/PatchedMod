package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.server.MinecraftServer;
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

	@ModifyVariable(
			at = @At(
					value = "NEW",
					target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;"
					),
			method = "*"
	)
	private MultiPackResourceManager patched$setupServerPatchTargetManager(MultiPackResourceManager manager) {
		DynamicPatches.setupTargetManager(PackType.SERVER_DATA, manager.listPacks().toList());
		return manager;
	}
}