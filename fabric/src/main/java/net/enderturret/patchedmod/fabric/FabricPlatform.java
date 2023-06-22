package net.enderturret.patchedmod.fabric;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.util.env.IPlatform;

public final class FabricPlatform implements IPlatform {

	private static final Logger LOGGER = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return LOGGER;
	}

	@Override
	public boolean isPhysicalClient() {
		return PatchedFabric.physicalClient;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public String getName(PackResources pack) {
		return pack instanceof ModResourcePack mod ? "mod/" + mod.getFabricModMetadata().getName() : pack.getName();
	}

	@Override
	public boolean isGroup(PackResources pack) {
		return false;//pack instanceof GroupResourcePackAccess;
	}

	@Override
	public Collection<PackResources> getChildren(PackResources pack) {
		return List.of();//pack instanceof GroupResourcePackAccess grpa ? transform(grpa.getPacks()) : List.of();
	}

	@Override
	public Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, ResourceLocation file) {
		return List.of();//pack instanceof GroupResourcePackAccess grpa ? transform(grpa.getNamespacedPacks().getOrDefault(file.getNamespace(), List.of())) : List.of();
	}

	private static Collection<PackResources> transform(List<ModResourcePack> list) {
		return list.stream().map(mrp -> (PackResources) mrp).toList();
	}
}