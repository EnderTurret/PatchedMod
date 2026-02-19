package net.enderturret.patchedmod.fabric;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.enderturret.patchedmod.common.env.IPatchingPackResources;
import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

public final class FabricPlatform implements PatchedPlatform {

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

	@Nullable
	public static ModMetadata getModMetadataFromPack(IPatchingPackResources pack) {
		if (pack instanceof IFabricModPackResources mod)
			return mod.patched$getFabricModMetadata();

		return null;
	}

	@Override
	@Nullable
	public PatchedMetadata deriveMetadataFromMod(PatchedPackResources pack) {
		final ModMetadata mod = getModMetadataFromPack(pack);
		if (mod == null) return null;

		final CustomValue cv = mod.getCustomValue("patched");
		if (cv == null) return null;

		return PatchedMetadata.of(cv, CustomValueOps.INSTANCE, mod.getName() + " (" + mod.getId() + ")");
	}

	@Override
	public PatchedResourceLocation tryParse(String input) {
		return (PatchedResourceLocation) (Object) Identifier.parse(input);
	}

	@Override
	public PatchedResourceLocation tryBuild(String namespace, String path) {
		return (PatchedResourceLocation) (Object) Identifier.tryBuild(namespace, path);
	}

	@Override
	public boolean isThingRegistered(PatchedResourceLocation registry, PatchedResourceLocation id) {
		final Registry<?> reg = BuiltInRegistries.REGISTRY.getValue((Identifier) (Object) registry);
		return reg != null && reg.containsKey((Identifier) (Object) id);
	}

	@Override
	public boolean isItemRegistered(PatchedResourceLocation id) {
		return BuiltInRegistries.ITEM.containsKey((Identifier) (Object) id);
	}
}