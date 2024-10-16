package net.enderturret.patchedmod.mixin.fabric.api;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;

@Mixin(GroupResourcePack.class)
public interface GroupResourcePackAccess {

	@Accessor(value = "packs", remap = false)
	public List<ModResourcePack> patched$getPacks();

	@Accessor(value = "namespacedPacks", remap = false)
	public Map<String, List<ModResourcePack>> patched$getNamespacedPacks();
}