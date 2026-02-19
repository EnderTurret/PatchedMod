package net.enderturret.patchedmod.mixin.fabric.api;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;

import net.enderturret.patchedmod.common.util.IPatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;
import net.enderturret.patchedmod.mixin.MixinAbstractPackResources;

/**
 * Identical to {@link MixinAbstractPackResources}.
 * @author EnderTurret
 */
@Mixin({ ModNioPackResources.class })
public abstract class MixinModNioResourcePack implements IPatchingPackResources {

	@Nullable
	private PatchedMetadata patched$meta;

	@Override
	public PatchedMetadata patchedMetadata() {
		checkInitialized();
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
	public boolean initialized() {
		return patched$meta != null;
	}
}