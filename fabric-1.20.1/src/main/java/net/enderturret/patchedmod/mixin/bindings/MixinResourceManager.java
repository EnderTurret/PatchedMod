package net.enderturret.patchedmod.mixin.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.env.PatchedResourceManager;

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
		return ((ResourceManager) this).listPacks()
				.map(p -> (PatchedPackResources) p)
				.filter(p -> p.patchedMetadata().patchingEnabled());
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
		return (PatchedPackResources) ((FallbackResourceManager) this).fallbacks.get(index).resources();
	}

	//

	@Override
	public default Set<String> patched$getNamespaces() {
		return ((ResourceManager) this).getNamespaces();
	}

	@Override
	public default Stream<PatchedPackResources> patched$listPacks() {
		return (Stream) ((ResourceManager) this).listPacks();
	}

	@Override
	public default Optional<InputStream> patched$getResource(PatchedResourceLocation location) throws IOException {
		final Optional<Resource> optional = ((ResourceManager) this).getResource((ResourceLocation) (Object) location);
		if (optional.isEmpty()) return Optional.empty();
		return Optional.of(optional.get().open());
	}
}