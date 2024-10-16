package net.enderturret.patchedmod.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.util.MixinCallbacks;

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
		MixinCallbacks.setupTargetManager(PackType.CLIENT_RESOURCES, packsByPriority);
		return packsByPriority;
	}

	@ModifyVariable(
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;"
					),
			method = "reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;",
			ordinal = 0
	)
	private List<PackResources> patched$setupClientPatchTargetManagerReload(List<PackResources> packsByPriority) {
		MixinCallbacks.setupTargetManager(PackType.CLIENT_RESOURCES, packsByPriority);
		return packsByPriority;
	}
}