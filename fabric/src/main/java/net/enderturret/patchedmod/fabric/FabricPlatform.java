package net.enderturret.patchedmod.fabric;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

import net.enderturret.patchedmod.util.env.IPlatform;
import net.enderturret.patchedmod.util.meta.PatchedMetadata;

final class FabricPlatform implements IPlatform {

	private final Logger logger = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return logger;
	}

	@Override
	public boolean isPhysicalClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
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
		final ModMetadata mod = getModMetadataFromPack(pack);
		if (mod != null) {
			final String modId = mod.getId();
			final String packId;

			if (!modId.equals(pack.packId()))
				if (pack.packId().startsWith(modId)) {
					final String temp = pack.packId().substring(modId.length());
					packId = temp.startsWith(":") ? temp.substring(1) : temp;
				} else
					packId = pack.packId();
			else
				packId = null;

			return "mod/" + mod.getName() + (packId != null ? "/" + packId : "");
		}

		return pack.packId();
	}

	@Nullable
	private static ModMetadata getModMetadataFromPack(PackResources pack) {
		if (pack instanceof IFabricModPackResources mod)
			return mod.patched$getFabricModMetadata();

		return null;
	}

	@Override
	@Nullable
	public PatchedMetadata deriveMetadataFromMod(PackResources pack) {
		final ModMetadata mod = getModMetadataFromPack(pack);
		if (mod == null) return null;

		final CustomValue cv = mod.getCustomValue("patched");
		if (cv == null) return null;

		return PatchedMetadata.of(cv, CustomValueOps.INSTANCE, mod.getName() + " (" + mod.getId() + ")");
	}

	@Override
	public boolean needsSwapNamespaceAndPath(PackResources pack) {
		// Fabric implementations surprisingly throw no errors, unlike Minecraft.
		return !isGroup(pack) && !(pack instanceof IFabricModPackResources);
	}

	@Override
	public Function<ResourceLocation, ResourceLocation> getRenamer(PackResources pack, String namespace) {
		// GroupResourcePack and ModNioResourcePack
		if (!needsSwapNamespaceAndPath(pack)) return Function.identity();
		// PathPackResources:      :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:  .:minecraft/something → minecraft:something
		return rl -> ResourceLocation.fromNamespaceAndPath(namespace, rl.getPath().substring(namespace.length() + 1));
	}
}