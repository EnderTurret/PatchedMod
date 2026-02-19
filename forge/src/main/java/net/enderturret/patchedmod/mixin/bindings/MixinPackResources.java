package net.enderturret.patchedmod.mixin.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;

import net.neoforged.fml.ModContainer;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.forge.ForgePlatform;
import net.enderturret.patchedmod.mixin.FilePackResourcesAccess;
import net.enderturret.patchedmod.mixin.SharedZipFileAccessAccess;

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
		return ((SharedZipFileAccessAccess) ((FilePackResourcesAccess) this).patched$getZipFileAccess()).patched$getOrCreateZipFile();
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
		final IoSupplier<InputStream> ret = ((PackResources) this).getResource(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA), (Identifier) (Object) location);
		return ret != null ? ret.get() : null;
	}

	@Override
	public default void patched$listResources(PatchedPackType type, String namespace, String path, Consumer<PatchedResourceLocation> consumer) {
		((PackResources) this).listResources(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA),
				namespace, path,
				(loc, io) -> consumer.accept((PatchedResourceLocation) (Object) loc));
	}

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

	@Override
	public default Function<PatchedResourceLocation, PatchedResourceLocation> patched$getRenamer(String namespace) {
		final boolean vanilla = patched$isVanillaPack();
		final int prefixLen = 0;
		// PathPackResources:     :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:  :minecraft/something → minecraft:something
		return rl -> (PatchedResourceLocation) (Object) Identifier.fromNamespaceAndPath(
				namespace, rl.patched$getPath().substring(prefixLen + namespace.length() + 1));
	}
}