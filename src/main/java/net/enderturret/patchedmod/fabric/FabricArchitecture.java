package net.enderturret.patchedmod.fabric;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.mixin.fabric.GroupResourcePackAccess;
import net.enderturret.patchedmod.util.env.IArchitecture;

final class FabricArchitecture implements IArchitecture {

	private final Logger logger = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return logger;
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
	public boolean isGroup(PackResources pack) {
		return pack instanceof GroupResourcePackAccess;
	}

	@Override
	public Collection<PackResources> getChildren(PackResources pack) {
		return pack instanceof GroupResourcePackAccess grpa ? transform(grpa.getPacks()) : List.of();
	}

	@Override
	public boolean needsSwapNamespaceAndPath(PackResources pack) {
		// Fabric implementations surprisingly throw no errors, unlike Minecraft.
		return !isGroup(pack) && !(pack instanceof ModNioResourcePack);
	}

	@Override
	public Function<ResourceLocation, ResourceLocation> getRenamer(PackResources pack, String namespace) {
		// GroupResourcePack and ModNioResourcePack
		if (!needsSwapNamespaceAndPath(pack)) return rl -> rl;
		// PathPackResources:      :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:  .:minecraft/something → minecraft:something
		return rl -> new ResourceLocation(namespace, rl.getPath().substring(namespace.length() + 1));
	}

	@Override
	public Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, ResourceLocation file) {
		return pack instanceof GroupResourcePackAccess grpa ? transform(grpa.getNamespacedPacks().getOrDefault(file.getNamespace(), List.of())) : List.of();
	}

	private static Collection<PackResources> transform(List<ModResourcePack> list) {
		return list.stream().map(mrp -> (PackResources) mrp).toList();
	}
}