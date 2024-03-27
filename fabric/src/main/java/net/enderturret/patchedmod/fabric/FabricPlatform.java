package net.enderturret.patchedmod.fabric;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.util.env.IPlatform;

final class FabricPlatform implements IPlatform {

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
	public boolean isModLoaded(String modId, String version) {
		final Version version2;
		try {
			version2 = Version.parse(version);
		} catch (VersionParsingException e) {
			return false;
		}

		return FabricLoader.getInstance().getModContainer(modId)
				.map(mc -> mc.getMetadata().getVersion().compareTo(version2))
				.orElse(-1) >= 0;
	}

	@Override
	public PackOutput getPackOutput(DataGenerator generator) {
		return generator.vanillaPackOutput;
	}

	@Override
	public String getName(PackResources pack) {
		if (pack instanceof ModResourcePack mod) {
			final String modId = mod.getFabricModMetadata().getId();
			final String packId;

			if (!modId.equals(pack.packId()))
				if (pack.packId().startsWith(modId)) {
					final String temp = pack.packId().substring(modId.length());
					packId = temp.startsWith(":") ? temp.substring(1) : temp;
				} else
					packId = pack.packId();
			else
				packId = null;

			return "mod/" + mod.getFabricModMetadata().getName() + (packId != null ? "/" + packId : "");
		}

		return pack.packId();
	}

	@Override
	public boolean isGroup(PackResources pack) {
		return false;
	}

	@Override
	public Collection<PackResources> getChildren(PackResources pack) {
		return List.of();
	}

	@Override
	public Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, ResourceLocation file) {
		return List.of();
	}

	private static Collection<PackResources> transform(List<ModResourcePack> list) {
		return list.stream().map(mrp -> (PackResources) mrp).toList();
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
}