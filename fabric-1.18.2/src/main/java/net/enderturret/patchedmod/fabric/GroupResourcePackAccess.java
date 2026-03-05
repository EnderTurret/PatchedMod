package net.enderturret.patchedmod.fabric;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;

import net.minecraft.server.packs.PackResources;

/**
 * Provides access to {@link GroupResourcePack#namespacedPacks} and {@link GroupResourcePack#packs}.
 * @author EnderTurret
 */
@Internal
public interface GroupResourcePackAccess {

	/**
	 * Provides access to {@link GroupResourcePack#packs}.
	 * @return The list of delegate packs.
	 */
	public List<? extends PackResources> patched$packs();

	/**
	 * Provides access to {@link GroupResourcePack#namespacedPacks}.
	 * @return The map of packs by namespace.
	 */
	public Map<String, List<PackResources>> patched$namespacedPacks();
}