package net.enderturret.patchedmod.mixin.quilt.api;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;
import org.spongepowered.asm.mixin.Mixin;

import net.enderturret.patchedmod.mixin.MixinAbstractPackResources;
import net.enderturret.patchedmod.util.IPatchingPackResources;
import net.enderturret.patchedmod.util.meta.PatchedMetadata;

/**
 * Identical to {@link MixinAbstractPackResources}.
 * @author EnderTurret
 */
@Mixin({ GroupResourcePack.class, ModNioResourcePack.class })
public abstract class MixinQuiltResourcePacks implements IPatchingPackResources {

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