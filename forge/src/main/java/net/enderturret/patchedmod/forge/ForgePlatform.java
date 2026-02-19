package net.enderturret.patchedmod.forge;

import java.util.Optional;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

public final class ForgePlatform implements PatchedPlatform {

	@Override
	public boolean isPhysicalClient() {
		return FMLEnvironment.getDist() == Dist.CLIENT;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isModLoaded(String modId, String version) {
		return ModList.get().getModContainerById(modId)
				.map(mc -> {
					final ArtifactVersion theirVersion = mc.getModInfo().getVersion();
					final DefaultArtifactVersion realVersion = new DefaultArtifactVersion(version);
					return theirVersion.compareTo(realVersion);
				})
				.orElse(-1) >= 0;
	}

	public static Optional<? extends ModContainer> findModNameFromModFile(PatchedPackResources pack) {
		if (pack.patched$packId().startsWith("mod/")) {
			final String modId = pack.patched$packId().substring("mod/".length());
			return ModList.get().getModContainerById(modId);
		}

		return Optional.empty();
	}

	@Override
	@Nullable
	public PatchedMetadata deriveMetadataFromMod(PatchedPackResources pack) {
		final Optional<? extends ModContainer> owningMod = findModNameFromModFile(pack);
		if (owningMod.isPresent()) {
			final ModContainer mod = owningMod.get();
			final Object obj = mod.getModInfo().getModProperties().get("patched");
			if (obj instanceof UnmodifiableConfig cfg)
				return PatchedMetadata.of(
						cfg,
						NightConfigOps.INSTANCE,
						mod.getModInfo().getDisplayName() + " (" + mod.getModInfo().getModId() + ")");
		}

		return null;
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