package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;

import net.minecraft.server.packs.AbstractPackResources;

import net.enderturret.patchedmod.util.IPatchingPackResources;

/**
 * Provides an {@link IPatchingPackResources} implementation for {@link AbstractPackResources}.
 * @author EnderTurret
 */
@Mixin({ AbstractPackResources.class, GroupResourcePack.class, ModNioResourcePack.class })
public abstract class MixinAbstractPackResources implements IPatchingPackResources {

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