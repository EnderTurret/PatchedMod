package net.enderturret.patchedmod.mixin.dynamic_patches;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.packs.AbstractPackResources;

/**
 * Removes {@link AbstractPackResources}'s check for slashes in calls to {@code getRootResource()},
 * so that Patched can resolve patches in the {@code patches} directory.
 * @author EnderTurret
 */
@Mixin(AbstractPackResources.class)
public abstract class MixinAbstractPackResources {

	@ModifyExpressionValue(at = @At(value = "INVOKE", target = "Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z"), method = "getRootResource")
	private boolean patched$allowSlashesForRootResource(boolean original) {
		return false;
	}
}