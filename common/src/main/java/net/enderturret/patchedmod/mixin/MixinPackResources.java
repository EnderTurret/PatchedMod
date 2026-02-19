package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.VanillaPackResources;

import net.enderturret.patchedmod.common.env.IPatchingPackResources;

@Mixin(PackResources.class)
public interface MixinPackResources extends IPatchingPackResources {

	@Override
	public default String patched$packId() {
		return ((PackResources) this).packId();
	}

	@Override
	public default boolean patched$isVanillaPack() {
		return this instanceof VanillaPackResources;
	}
}