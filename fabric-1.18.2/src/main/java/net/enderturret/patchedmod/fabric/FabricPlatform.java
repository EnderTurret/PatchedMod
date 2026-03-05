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

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import net.enderturret.patchedmod.common.env.PatchingPackResources;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

/**
 * The Fabric {@code PatchedPlatform} implementation.
 * @author EnderTurret
 */
public final class FabricPlatform implements PatchedPlatform {

	@Override
	public boolean hasGroupPacks() {
		return true; // Unfortunately.
	}

	@Override
	public boolean hasLegacyPatchedMetadata() {
		return true;
	}

	@Override
	public boolean hasUnprefixedPackIds() {
		return true;
	}

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

	/**
	 * Retrieves the mod metadata from the specified pack, or returns {@code null} if the pack does not represent a mod.
	 * @param pack The pack to retrieve the mod metadata from.
	 * @return The mod metadata, or {@code null}.
	 */
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
		return result.result().isPresent() ? new JankyDataResult<>(result.result().get(), null) : new JankyDataResult<>(null, result.error().get().message());
	}

	@Override
	public <T> DataResult<T> success(T value) {
		return DataResult.success(value);
	}

	@Override
	public <T> DataResult<T> error(Supplier<String> message) {
		return DataResult.error(message.get());
	}

	@Override
	public PatchedResourceLocation tryParse(String input) {
		return (PatchedResourceLocation) ResourceLocation.tryParse(input);
	}

	@Override
	public PatchedResourceLocation tryBuild(String namespace, String path) {
		try {
			return (PatchedResourceLocation) new ResourceLocation(namespace, path);
		} catch (ResourceLocationException e) {
			return null;
		}
	}

	@Override
	public boolean isThingRegistered(PatchedResourceLocation registry, PatchedResourceLocation id) {
		final Registry<?> reg = Registry.REGISTRY.get((ResourceLocation) registry);
		return reg != null && reg.containsKey((ResourceLocation) id);
	}

	@Override
	public boolean isItemRegistered(PatchedResourceLocation id) {
		return Registry.ITEM.containsKey((ResourceLocation) id);
	}
}