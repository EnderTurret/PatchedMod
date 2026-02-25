package net.enderturret.patchedmod.mixin.bindings;

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
import net.minecraft.server.packs.resources.IoSupplier;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.resource.DelegatingPackResources;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.forge.ForgePlatform;
import net.enderturret.patchedmod.forge.PatchedVersionHacks;

@Mixin(PackResources.class)
public interface MixinPackResources extends PatchedPackResources {

	@Override
	public default String patched$packId() {
		return ((PackResources) this).packId();
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
		final IoSupplier<InputStream> ret = ((PackResources) this).getRootResource(path);
		return ret != null ? ret.get() : null;
	}

	@Override
	public default @Nullable InputStream patched$getResource(PatchedPackType type, PatchedResourceLocation location) throws IOException {
		final IoSupplier<InputStream> ret = ((PackResources) this).getResource(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA), (ResourceLocation) location);
		return ret != null ? ret.get() : null;
	}

	@Override
	public default void patched$listResources(PatchedPackType type, String namespace, String path, Consumer<PatchedResourceLocation> consumer) {
		((PackResources) this).listResources(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA),
				namespace, path,
				(loc, io) -> consumer.accept((PatchedResourceLocation) loc));
	}

	@Override
	public default boolean patched$hasRootResource(String... path) {
		return ((PackResources) this).getRootResource(path) != null;
	}

	@Override
	public default boolean patched$hasResource(PatchedPackType type, PatchedResourceLocation location) {
		final IoSupplier<InputStream> ret = ((PackResources) this).getResource(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA), (ResourceLocation) location);
		return ret != null;
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
		return true;
	}

	@SuppressWarnings("removal")
	@Override
	public default Function<PatchedResourceLocation, PatchedResourceLocation> patched$getRenamer(String namespace) {
		final int prefixLen = patched$isVanillaPack() ? "../".length() + 1 : 1;

		// PathPackResources:        :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:     :../minecraft/something → minecraft:something

		return rl -> (PatchedResourceLocation) new ResourceLocation(
				namespace, rl.patched$getPath().substring(prefixLen + namespace.length()));
	}

	@Override
	public default boolean patched$isGroupPack() {
		return this instanceof DelegatingPackResources;
	}

	@Override
	public default Collection<PatchedPackResources> patched$getChildren() {
		return (Collection) Objects.requireNonNullElse(((PackResources) this).getChildren(), List.of());
	}

	@Override
	public default Collection<PatchedPackResources> patched$getFilteredChildren(PatchedPackType type, String namespace) {
		return (Collection) PatchedVersionHacks.getCandidatePacks((PackResources) this, type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA), namespace);
	}
}