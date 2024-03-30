package net.enderturret.patchedmod.mixin;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.packs.AbstractPackResources;

import net.enderturret.patchedmod.util.IPatchingPackResources;
import net.enderturret.patchedmod.util.meta.PatchedMetadata;

/**
 * Provides an {@link IPatchingPackResources} implementation for {@link AbstractPackResources}.
 * @author EnderTurret
 */
@Mixin(AbstractPackResources.class)
public abstract class MixinAbstractPackResources implements IPatchingPackResources {

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