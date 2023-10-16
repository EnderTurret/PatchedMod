package net.enderturret.patchedmod.quilt;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.mixin.quilt.GroupResourcePackAccess;
import net.enderturret.patchedmod.mixin.quilt.ModNioResourcePackAccess;
import net.enderturret.patchedmod.util.env.IPlatform;

final class QuiltPlatform implements IPlatform {

	private final Logger logger = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return logger;
	}

	@Override
	public boolean isPhysicalClient() {
		return PatchedQuilt.physicalClient;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return QuiltLoader.isModLoaded(modId);
	}

	@Override
	public boolean isModLoaded(String modId, String version) {
		return QuiltLoader.getModContainer(modId)
				.map(mc -> mc.metadata().version().compareTo(Version.of(version)))
				.orElse(-1) >= 0;
	}

	@Override
	public PackOutput getPackOutput(DataGenerator generator) {
		return generator.vanillaPackOutput;
	}

	@Override
	public String getName(PackResources pack) {
		if (pack instanceof ModNioResourcePackAccess mod) {
			final String modName = mod.getModInfo().name();
			final String packId;

			if (!modName.equals(pack.packId()))
				if (pack.packId().startsWith(modName)) {
					final String temp = pack.packId().substring(modName.length());
					packId = temp.startsWith(":") ? temp.substring(1) : temp;
				} else
					packId = pack.packId();
			else
				packId = null;

			return "mod/" + modName + (packId != null ? "/" + packId : "");
		}

		return pack.packId();
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
	public Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, ResourceLocation file) {
		return pack instanceof GroupResourcePackAccess grpa ? transform(grpa.getNamespacedPacks().getOrDefault(file.getNamespace(), List.of())) : List.of();
	}

	@SuppressWarnings("unchecked")
	private static Collection<PackResources> transform(List<? extends PackResources> list) {
		return (List<PackResources>) list;
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