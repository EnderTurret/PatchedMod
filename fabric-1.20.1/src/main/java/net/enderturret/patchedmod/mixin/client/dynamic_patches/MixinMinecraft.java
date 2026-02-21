package net.enderturret.patchedmod.mixin.client.dynamic_patches;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;

import net.enderturret.patchedmod.common.internal.PatchTargetManager;
import net.enderturret.patchedmod.common.internal.flow.DynamicPatches;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Handles setting up the resource pack {@link PatchTargetManager}.
 * @author EnderTurret
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

	@ModifyVariable(
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;"
					),
			method = "<init>",
			ordinal = 0
	)
	private List<PackResources> patched$setupClientPatchTargetManagerInitial(List<PackResources> packsByPriority) {
		DynamicPatches.setupTargetManager(PatchedPackType.CLIENT_RESOURCES, (List) packsByPriority);
		return packsByPriority;
	}

	@ModifyVariable(
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;"
					),
			method = "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;",
			ordinal = 0
	)
	private List<PackResources> patched$setupClientPatchTargetManagerReload(List<PackResources> packsByPriority) {
		DynamicPatches.setupTargetManager(PatchedPackType.CLIENT_RESOURCES, (List) packsByPriority);
		return packsByPriority;
	}
}