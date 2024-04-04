package net.enderturret.patchedmod.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.util.MixinCallbacks;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

	// These are both separate because I'm pretty sure Mixin will defenestrate me if I have a static injector on an instance target.

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;"),
			method = { "reloadResourcePacks" })
	private void patched$setupClientPatchTargetManager(List<PackResources> packsByPriority) {
		MixinCallbacks.setupTargetManager(PackType.CLIENT_RESOURCES, packsByPriority);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;"),
			method = { "<init>" })
	private static void patched$setupClientPatchTargetManagerStatic(List<PackResources> packsByPriority) {
		MixinCallbacks.setupTargetManager(PackType.CLIENT_RESOURCES, packsByPriority);
	}
}