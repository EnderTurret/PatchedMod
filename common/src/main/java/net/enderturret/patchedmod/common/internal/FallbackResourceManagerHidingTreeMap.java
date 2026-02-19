package net.enderturret.patchedmod.common.internal;

import java.util.TreeMap;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.enderturret.patchedmod.common.env.PatchedResourceManager;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * {@code FallbackResourceManagerHidingTreeMap} is, as the name suggests, a {@link TreeMap} that also tracks a {@code FallbackResourceManager}.
 * The purpose of this class is to allow passing a {@code FallbackResourceManager} into a static lambda by hiding it in one of the captured locals.
 *
 * @author EnderTurret
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 */
@Internal
public final class FallbackResourceManagerHidingTreeMap<K, V> extends TreeMap<K, V> {

	/**
	 * The resource manager being smuggled into {@code static} context.
	 */
	public final PatchedResourceManager manager;

	/**
	 * The {@linkplain #manager resource manager}'s pack type.
	 */
	public final PatchedPackType type;

	/**
	 * Constructs a new {@code FallbackResourceManagerHidingTreeMap} with the specified parameters.
	 * @param manager The resource manager.
	 * @param type The pack type.
	 */
	public FallbackResourceManagerHidingTreeMap(PatchedResourceManager manager, PatchedPackType type) {
		this.manager = manager;
		this.type = type;
	}
}