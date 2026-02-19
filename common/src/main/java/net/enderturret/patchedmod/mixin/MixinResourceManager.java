package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.common.env.PatchedResourceManager;

@Mixin(ResourceManager.class)
public interface MixinResourceManager extends PatchedResourceManager {

}