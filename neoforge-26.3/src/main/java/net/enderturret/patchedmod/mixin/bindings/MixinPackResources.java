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
import net.neoforged.neoforge.resource.JarContentsPackResources;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.mixin.command.FilePackResourcesAccess;
import net.enderturret.patchedmod.mixin.command.SharedZipFileAccessAccess;
import net.enderturret.patchedmod.neoforge.NeoForgePlatform;

/**
 * Implements common bindings for {@code PackResources}.
 * @author EnderTurret
 */
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
				(loc, _) -> consumer.accept((PatchedResourceLocation) (Object) loc));
	}

	@Override
	public default boolean patched$hasRootResource(String... path) {
		return ((PackResources) this).getRootResource(path) != null;
	}

	@Override
	public default boolean patched$hasResource(PatchedPackType type, PatchedResourceLocation location) {
		final IoSupplier<InputStream> ret = ((PackResources) this).getResource(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA), (Identifier) (Object) location);
		return ret != null;
	}

	//

	@Override
	public default String patched$getName() {
		final Optional<? extends ModContainer> mod = NeoForgePlatform.findModNameFromModFile(this);

		if (mod.isPresent())
			return "mod/" + mod.get().getModInfo().getDisplayName();

		return patched$packId();
	}

	@Override
	public default boolean patched$needsSwapNamespaceAndPath() {
		return true; // Not actually important for JarContentsPackResources — it'll be weirdly broken with or without this hack.
	}

	@Override
	public default Function<PatchedResourceLocation, PatchedResourceLocation> patched$getRenamer(String namespace) {
		final int prefixLen = this instanceof JarContentsPackResources ? 0 : 1;

		// PathPackResources:        :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:     :minecraft/something → minecraft:something
		// JarContentsPackResources: :inecraft/something  → minecraft:something
		// (The latter is because the path ends up being "assets//minecraft/" and NeoForge gets a little too excited trimming it.)

		return rl -> (PatchedResourceLocation) (Object) Identifier.fromNamespaceAndPath(
				namespace, rl.patched$getPath().substring(prefixLen + namespace.length()));
	}
}