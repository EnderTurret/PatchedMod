package net.enderturret.patchedmod.mixin.impl;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.packs.AbstractPackResources;

import net.enderturret.patchedmod.common.env.PatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

/**
 * Provides an {@link PatchingPackResources} implementation for {@link AbstractPackResources}.
 * @author EnderTurret
 */
@Mixin(AbstractPackResources.class)
public abstract class MixinAbstractPackResources implements PatchingPackResources {

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