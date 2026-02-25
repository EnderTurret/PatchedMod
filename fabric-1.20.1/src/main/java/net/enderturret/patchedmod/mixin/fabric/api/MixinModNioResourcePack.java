package net.enderturret.patchedmod.mixin.fabric.api;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

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