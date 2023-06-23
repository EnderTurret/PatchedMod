package net.enderturret.patchedmod.mixin.quilt;

import java.util.List;
import java.util.Map;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.packs.PackResources;

@Mixin(GroupResourcePack.class)
public interface GroupResourcePackAccess {

	@Accessor(remap = false)
	public List<? extends PackResources> getPacks();

	@Accessor(remap = false)
	public Map<String, List<PackResources>> getNamespacedPacks();
}