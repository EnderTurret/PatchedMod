package net.enderturret.patchedmod.mixin;

import java.util.Set;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.common.env.IPatchingPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceManager;

@Mixin(ResourceManager.class)
public interface MixinResourceManager extends PatchedResourceManager {

	@Override
	public default Stream<IPatchingPackResources> patched$getExpandedPacks() {
		return ((ResourceManager) this).listPacks()
				.map(p -> (IPatchingPackResources) p)
				.flatMap(p -> p.patched$isGroupPack() ? p.patched$getChildren().stream() : Stream.of(p));
	}

	@Override
	public default Stream<IPatchingPackResources> patched$getPatchingPacks() {
		return ((ResourceManager) this).listPacks()
				.map(p -> (IPatchingPackResources) p)
				.filter(p -> p.patchedMetadata().patchingEnabled());
	}

	@Override
	public default Set<String> patched$getNamespaces() {
		return ((ResourceManager) this).getNamespaces();
	}

	@Override
	public default Stream<IPatchingPackResources> patched$listPacks() {
		return (Stream) ((ResourceManager) this).listPacks();
	}
}