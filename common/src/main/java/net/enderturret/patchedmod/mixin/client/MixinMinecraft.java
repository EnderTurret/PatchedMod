package net.enderturret.patchedmod.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.util.MixinCallbacks;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;"),
			method = { "<init>", "reloadResourcePacks" })
	private void patched$setupClientPatchTargetManager(List<PackResources> packsByPriority, CallbackInfo ci) {
		MixinCallbacks.setupTargetManager(PackType.CLIENT_RESOURCES, packsByPriority);
	}
}