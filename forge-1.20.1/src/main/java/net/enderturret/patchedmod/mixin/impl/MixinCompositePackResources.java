package net.enderturret.patchedmod.mixin.impl;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import net.minecraft.server.packs.AbstractPackResources;

import net.enderturret.patchedmod.common.env.PatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

/**
 * Provides an {@link PatchingPackResources} implementation for {@code CompositePackResources} (from ≥1.20.2).
 * @author EnderTurret
 */
@Pseudo
@Mixin(targets = { "net/minecraft/server/packs/CompositePackResources", "net/minecraft/class_8614" })
public abstract class MixinCompositePackResources implements PatchingPackResources {

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