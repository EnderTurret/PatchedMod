package net.enderturret.patchedmod.fabric;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.enderturret.patchedmod.common.env.PatchingPackResources;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

public final class FabricPlatform implements PatchedPlatform {

	@Override
	public boolean isPhysicalClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	@Override
	public boolean isProduction() {
		return !FabricLoader.getInstance().isDevelopmentEnvironment();
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
	public static ModMetadata getModMetadataFromPack(PatchingPackResources pack) {
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
		return (PatchedResourceLocation) (Object) Identifier.tryParse(input);
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