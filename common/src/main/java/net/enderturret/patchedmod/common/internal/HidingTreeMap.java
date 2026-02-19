package net.enderturret.patchedmod.common.internal;

import java.util.TreeMap;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * {@code HidingTreeMap} is a {@link TreeMap} that also tracks an object ({@code T}).
 * The purpose of this class is to allow passing a {@code FallbackResourceManager} into a static lambda by hiding it in one of the captured locals.
 *
 * @author EnderTurret
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @param <T> The type of hidden object.
 */
@Internal
public final class HidingTreeMap<K, V, T> extends TreeMap<K, V> {

	/**
	 * The object being smuggled into {@code static} context.
	 */
	public final T value;

	/**
	 * Constructs a new {@code HidingTreeMap} with the specified parameters.
	 * @param value The value.
	 */
	public HidingTreeMap(T value) {
		this.value = value;
	}
}