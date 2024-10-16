package net.enderturret.patchedmod.mixin.quilt.api;

import java.util.List;
import java.util.Map;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.packs.PackResources;

@Mixin(GroupResourcePack.class)
public interface GroupResourcePackAccess {

	@Accessor(value = "packs", remap = false)
	public List<? extends PackResources> patched$getPacks();

	@Accessor(value = "namespacedPacks", remap = false)
	public Map<String, List<PackResources>> patched$getNamespacedPacks();
}