package net.enderturret.patchedmod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.util.MixinCallbacks;

@Mixin(WorldLoader.PackConfig.class)
public abstract class MixinPackConfig {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;"),
			method = { "createResourceManager" })
	private void patched$setupServerPatchTargetManager(List<PackResources> packsByPriority) {
		MixinCallbacks.setupTargetManager(PackType.SERVER_DATA, packsByPriority);
	}
}