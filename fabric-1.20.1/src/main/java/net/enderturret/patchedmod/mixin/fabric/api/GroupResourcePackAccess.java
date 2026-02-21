package net.enderturret.patchedmod.mixin.fabric.api;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;

import net.minecraft.server.packs.PackResources;

@Mixin(value = GroupResourcePack.class, remap = false)
public interface GroupResourcePackAccess {

	@Accessor("packs")
	public List<? extends PackResources> patched$packs();

	@Accessor("namespacedPacks")
	public Map<String, List<PackResources>> patched$namespacedPacks();
}