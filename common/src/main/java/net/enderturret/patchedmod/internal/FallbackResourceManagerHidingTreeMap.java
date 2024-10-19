package net.enderturret.patchedmod.internal;

import java.util.TreeMap;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;

/**
 * {@code FallbackResourceManagerHidingTreeMap} is, as the name suggests, a {@link TreeMap} that also tracks a {@link FallbackResourceManager}.
 * The purpose of this class is to allow passing a {@code FallbackResourceManager} into a static lambda by hiding it in one of the captured locals.
 *
 * @author EnderTurret
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 */
@Internal
public final class FallbackResourceManagerHidingTreeMap<K, V> extends TreeMap<K, V> {

	public final FallbackResourceManager manager;
	public final PackType type;

	public FallbackResourceManagerHidingTreeMap(FallbackResourceManager manager, PackType type) {
		this.manager = manager;
		this.type = type;
	}
}