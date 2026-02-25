package net.enderturret.patchedmod.mixin.bindings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.loader.api.metadata.ModMetadata;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.fabric.FabricPlatform;
import net.enderturret.patchedmod.fabric.GroupResourcePackAccess;
import net.enderturret.patchedmod.fabric.IFabricModPackResources;
import net.enderturret.patchedmod.fabric.PatchedVersionHacks;

@Mixin(PackResources.class)
public interface MixinPackResources extends PatchedPackResources {

	@Override
	public default String patched$packId() {
		return ((PackResources) this).getName();
	}

	@Override
	public default boolean patched$isVanillaPack() {
		return this instanceof VanillaPackResources;
	}

	@Override
	public default boolean patched$isFilePack() {
		return this instanceof FilePackResources;
	}

	@Override
	public default ZipFile patched$getFilePackZipFile() {
		return PatchedVersionHacks.getOrCreateZipFile((FilePackResources) this);
	}

	@Override
	public default Set<String> patched$getNamespaces(PatchedPackType type) {
		return ((PackResources) this).getNamespaces(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA));
	}

	@Override
	public default @Nullable InputStream patched$getRootResource(String... path) throws IOException {
		if (path.length > 1 && patched$isVanillaPack()) return null;
		try {
			return ((PackResources) this).getRootResource(String.join("/", path));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	@Override
	public default @Nullable InputStream patched$getResource(PatchedPackType type, PatchedResourceLocation location) throws IOException {
		try {
			return ((PackResources) this).getResource(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA), (ResourceLocation) location);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	@Override
	public default void patched$listResources(PatchedPackType type, String namespace, String path, Consumer<PatchedResourceLocation> consumer) {
		((PackResources) this).getResources(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA),
				namespace, path,
				loc -> { consumer.accept((PatchedResourceLocation) loc); return false; });
	}

	@Override
	public default boolean patched$hasRootResource(String... path) {
		try {
			final InputStream stream = patched$getRootResource(path);
			if (stream != null) {
				stream.close();
				return true;
			}
		} catch (IOException ignored) {}
		return false;
	}

	@Override
	public default boolean patched$hasResource(PatchedPackType type, PatchedResourceLocation location) {
		return ((PackResources) this).hasResource(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA), (ResourceLocation) location);
	}

	//

	@Override
	public default String patched$getName() {
		final ModMetadata mod = FabricPlatform.getModMetadataFromPack(this);
		if (mod != null) {
			final String modId = mod.getName();
			final String packId;

			if (!modId.equals(patched$packId()))
				if (patched$packId().startsWith(modId)) {
					final String temp = patched$packId().substring(modId.length());
					packId = temp.startsWith(":") ? temp.substring(1) : temp;
				} else
					packId = patched$packId();
			else
				packId = null;

			return "mod/" + mod.getName() + (packId != null ? "/" + packId : "");
		}

		return patched$packId();
	}

	@Override
	public default boolean patched$needsSwapNamespaceAndPath() {
		// Fabric implementations surprisingly throw no errors, unlike Minecraft.
		return !patched$isGroupPack() && !(this instanceof IFabricModPackResources);
	}

	@Override
	public default Function<PatchedResourceLocation, PatchedResourceLocation> patched$getRenamer(String namespace) {
		// GroupResourcePack and ModNioResourcePack
		if (!patched$needsSwapNamespaceAndPath()) return Function.identity();
		// PathPackResources:      :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:  .:minecraft/something → minecraft:something
		return rl -> (PatchedResourceLocation) new ResourceLocation(
				namespace, rl.patched$getPath().substring(namespace.length() + 1));
	}

	@Override
	public default boolean patched$isGroupPack() {
		return this instanceof GroupResourcePackAccess; // Don't reference GroupResourcePack directly, for Quilt "support".
	}

	@Override
	public default Collection<PatchedPackResources> patched$getChildren() {
		return (Collection) ((GroupResourcePackAccess) this).patched$packs();
	}

	@Override
	public default Collection<PatchedPackResources> patched$getFilteredChildren(PatchedPackType type, String namespace) {
		return (Collection) ((GroupResourcePackAccess) this).patched$namespacedPacks().get(namespace);
	}
}