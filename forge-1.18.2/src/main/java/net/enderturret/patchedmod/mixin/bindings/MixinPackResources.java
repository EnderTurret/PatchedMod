package net.enderturret.patchedmod.mixin.bindings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.resource.DelegatingResourcePack;
import net.minecraftforge.resource.PathResourcePack;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.forge.ForgePlatform;
import net.enderturret.patchedmod.forge.PatchedVersionHacks;
import net.enderturret.patchedmod.mixin.forge.DelegatingPackResourcesAccess;

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
		final Collection<ResourceLocation> resources = ((PackResources) this).getResources(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA),
				namespace, path, Integer.MAX_VALUE,
				// Pre-filter to json files and patches.
				str -> str.endsWith(".json") || str.endsWith(".patch"));

		for (ResourceLocation rl : resources)
			consumer.accept((PatchedResourceLocation) rl);
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
		final Optional<? extends ModContainer> mod = ForgePlatform.findModNameFromModFile(this);

		if (mod.isPresent())
			return "mod/" + mod.get().getModInfo().getDisplayName();

		return patched$packId();
	}

	@Override
	public default boolean patched$needsSwapNamespaceAndPath() {
		return !patched$isGroupPack() && !(this instanceof PathResourcePack);
	}

	@SuppressWarnings("removal")
	@Override
	public default Function<PatchedResourceLocation, PatchedResourceLocation> patched$getRenamer(String namespace) {
		if (!patched$needsSwapNamespaceAndPath()) return Function.identity();

		final int prefixLen = patched$isVanillaPack() ? "../".length() + 1 : 1;

		// PathPackResources:        :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:     :../minecraft/something → minecraft:something

		return rl -> (PatchedResourceLocation) new ResourceLocation(
				namespace, rl.patched$getPath().substring(prefixLen + namespace.length()));
	}

	@Override
	public default boolean patched$isGroupPack() {
		return this instanceof DelegatingResourcePack;
	}

	@Override
	public default Collection<PatchedPackResources> patched$getChildren() {
		return (Collection) Objects.requireNonNullElse(((DelegatingPackResourcesAccess) this).patched$delegates(), List.of());
	}

	@Override
	public default Collection<PatchedPackResources> patched$getFilteredChildren(PatchedPackType type, String namespace) {
		return (Collection) PatchedVersionHacks.getCandidatePacks((PackResources) this, type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA), namespace);
	}
}