package net.enderturret.patchedmod.mixin.dynamic_patches;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

import net.enderturret.patchedmod.common.env.PatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchTarget;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

@Mixin(MultiPackResourceManager.class)
public abstract class MixinMultiPackResourceManager {

	@ModifyExpressionValue(at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"), method = "<init>")
	private boolean patched$includeDynamicPacks(boolean original, PackType type, List<PackResources> packs,
			@Local(ordinal = 0, index = 6) PackResources pack,
			@Local(ordinal = 0, index = 11) String namespace) {
		if (original) return true;

		if (pack instanceof PatchingPackResources ppp && ppp.patched$hasPatches()
				&& ppp.patchedMetadata().hasDynamicNamespace(PatchedPackType.fromVanilla(PackType.CLIENT_RESOURCES, type), namespace))
			return true;

		return false; // original must be false here.
	}
}