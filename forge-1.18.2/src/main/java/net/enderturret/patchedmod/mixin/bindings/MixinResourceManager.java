package net.enderturret.patchedmod.mixin.bindings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;

@Mixin(ResourceManager.class)
public interface MixinResourceManager extends PatchedResourceManager {

	@Override
	public default Stream<PatchedPackResources> patched$getExpandedPacks() {
		return ((ResourceManager) this).listPacks()
				.map(p -> (PatchedPackResources) p)
				.flatMap(p -> p.patched$isGroupPack() ? p.patched$getChildren().stream() : Stream.of(p));
	}

	@Override
	public default Stream<PatchedPackResources> patched$getPatchingPacks() {
		return patched$getExpandedPacks().filter(p -> p.patchedMetadata().patchingEnabled());
	}

	@Override
	public default boolean patched$isFallback() {
		return this instanceof FallbackResourceManager;
	}

	@Override
	public default int patched$getFallbackPackCount() {
		return ((FallbackResourceManager) this).fallbacks.size();
	}

	@Override
	public default PatchedPackResources patched$getFallbackPack(int index) {
		return (PatchedPackResources) ((FallbackResourceManager) this).fallbacks.get(index);
	}

	//

	@Override
	public default Set<String> patched$getNamespaces() {
		return ((ResourceManager) this).getNamespaces();
	}

	@Override
	public default Stream<PatchedPackResources> patched$listPacks() {
		return patched$getExpandedPacks();
	}

	@Override
	public default Optional<InputStream> patched$getResource(PatchedResourceLocation location) throws IOException {
		try {
			final Resource resource = ((ResourceManager) this).getResource((ResourceLocation) location);
			return Optional.of(resource.getInputStream());
		} catch (FileNotFoundException e) {
			return Optional.empty();
		}
	}

	@Override
	public default Optional<List<InputStream>> patched$getResourceStack(PatchedResourceLocation location) throws IOException {
		final List<Resource> list = ((ResourceManager) this).getResources((ResourceLocation) location);
		if (list.isEmpty()) return Optional.empty();
		return Optional.of(list.stream().map(Resource::getInputStream).toList());
	}
}