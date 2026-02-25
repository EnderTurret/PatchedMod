package net.enderturret.patchedmod.neoforge;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform.JankyDataResult;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

public final class NeoForgePlatform implements PatchedPlatform {

	@Override
	public boolean isPhysicalClient() {
		return FMLEnvironment.dist == Dist.CLIENT;
	}

	@Override
	public boolean isProduction() {
		return FMLEnvironment.production;
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
	public <T, I> JankyDataResult<T> decode(Codec<T> codec, DynamicOps<I> ops, I input) {
		final DataResult<T> result = codec.parse(ops, input);
		return result.isSuccess() ? new JankyDataResult<>(result.getOrThrow(), null) : new JankyDataResult<>(null, result.error().get().message());
	}

	@Override
	public <T> DataResult<T> success(T value) {
		return DataResult.success(value);
	}

	@Override
	public <T> DataResult<T> error(Supplier<String> message) {
		return DataResult.error(message);
	}

	@Override
	public PatchedResourceLocation tryParse(String input) {
		return (PatchedResourceLocation) (Object) ResourceLocation.tryParse(input);
	}

	@Override
	public PatchedResourceLocation tryBuild(String namespace, String path) {
		return (PatchedResourceLocation) (Object) ResourceLocation.tryBuild(namespace, path);
	}

	@Override
	public boolean isThingRegistered(PatchedResourceLocation registry, PatchedResourceLocation id) {
		final Registry<?> reg = PatchedVersionHacks.get(BuiltInRegistries.REGISTRY, (ResourceLocation) (Object) registry);
		return reg != null && reg.containsKey((ResourceLocation) (Object) id);
	}

	@Override
	public boolean isItemRegistered(PatchedResourceLocation id) {
		return BuiltInRegistries.ITEM.containsKey((ResourceLocation) (Object) id);
	}
}