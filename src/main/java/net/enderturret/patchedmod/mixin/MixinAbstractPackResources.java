package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

import net.enderturret.patchedmod.util.IPatchingPackResources;

@Mixin(AbstractPackResources.class)
public abstract class MixinAbstractPackResources implements IPatchingPackResources {

	private Boolean hasPatches = null;

	@Override
	public boolean hasPatches() {
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