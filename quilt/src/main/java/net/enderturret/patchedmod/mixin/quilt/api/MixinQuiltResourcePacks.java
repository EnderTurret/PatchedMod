package net.enderturret.patchedmod.mixin.quilt.api;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;
import org.spongepowered.asm.mixin.Mixin;

import net.enderturret.patchedmod.mixin.MixinAbstractPackResources;
import net.enderturret.patchedmod.util.IPatchingPackResources;

/**
 * Identical to {@link MixinAbstractPackResources}.
 * @author EnderTurret
 */
@Mixin({ GroupResourcePack.class, ModNioResourcePack.class })
public class MixinQuiltResourcePacks implements IPatchingPackResources {

	private Boolean hasPatches = null;

	@Override
	public boolean hasPatches() {
		checkInitialized();
		return hasPatches != null && hasPatches;
	}

	@Override
	public boolean initialized() {
		return hasPatches != null;
	}

	@Override
	public void setHasPatches(boolean value) {
		if (hasPatches == null)
			hasPatches = value;
	}
}