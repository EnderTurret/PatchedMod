package net.enderturret.patchedmod.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;

import net.minecraft.server.packs.AbstractPackResources;

import net.enderturret.patchedmod.mixin.MixinAbstractPackResources;
import net.enderturret.patchedmod.util.IPatchingPackResources;

/**
 * Identical to {@link MixinAbstractPackResources}.
 * @author EnderTurret
 */
@Mixin({ GroupResourcePack.class, ModNioResourcePack.class })
public class MixinFabricResourcePacks implements IPatchingPackResources {

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