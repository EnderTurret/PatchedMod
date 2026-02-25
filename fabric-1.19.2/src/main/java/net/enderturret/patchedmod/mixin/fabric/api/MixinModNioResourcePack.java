package net.enderturret.patchedmod.mixin.fabric.api;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;

import net.enderturret.patchedmod.common.env.PatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;
import net.enderturret.patchedmod.mixin.impl.MixinAbstractPackResources;

/**
 * Identical to {@link MixinAbstractPackResources}.
 * @author EnderTurret
 */
@Mixin({ ModNioResourcePack.class })
public abstract class MixinModNioResourcePack implements PatchingPackResources {

	// Apply the same change as dynamic_patches.MixinAbstractPackResources.
	@ModifyExpressionValue(at = @At(value = "INVOKE", target = "Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z"), method = "getRootResource")
	private boolean patched$allowSlashesForRootResource(boolean original) {
		return false;
	}

	@Nullable
	private PatchedMetadata patched$meta;

	@Override
	public PatchedMetadata patchedMetadata() {
		patched$checkInitialized();
		return patched$meta;
	}

	@Override
	public void setPatchedMetadata(PatchedMetadata value) {
		Objects.requireNonNull(value);
		if (patched$meta == null)
			patched$meta = value;
		else
			throw new UnsupportedOperationException();
	}

	@Override
	public boolean patched$initialized() {
		return patched$meta != null;
	}
}