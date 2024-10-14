package net.enderturret.patchedmod.mixin.fabric.api;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;

@Mixin(GroupResourcePack.class)
public interface GroupResourcePackAccess {

	@Accessor(remap = false)
	public List<ModResourcePack> getPacks();

	@Accessor(remap = false)
	public Map<String, List<ModResourcePack>> getNamespacedPacks();
}