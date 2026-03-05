package net.enderturret.patchedmod.mixin.fabric.api;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;

import net.minecraft.server.packs.PackResources;

import net.enderturret.patchedmod.fabric.GroupResourcePackAccess;

@Mixin(value = GroupResourcePack.class, remap = false)
public abstract class MixinGroupResourcePack implements GroupResourcePackAccess {

	@Shadow
	@Final
	private List<? extends PackResources> packs;
	@Shadow
	@Final
	private Map<String, List<PackResources>> namespacedPacks;

	@Override
	public List<? extends PackResources> patched$packs() {
		return packs;
	}

	@Override
	public Map<String, List<PackResources>> patched$namespacedPacks() {
		return namespacedPacks;
	}
}