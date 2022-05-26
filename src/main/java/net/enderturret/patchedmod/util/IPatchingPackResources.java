package net.enderturret.patchedmod.util;

public interface IPatchingPackResources {

	/**
	 * Whether the pack has any patches.
	 * This is an optimization for situations with a lot of mods and hardly any patches (if any).
	 * @return {@code true} if this resource/data pack has patches.
	 */
	public default boolean hasPatches() {
		return false;
	}

	/**
	 * @return {@code true} if the Patched metadata has been initialized for this pack.
	 */
	public default boolean initialized() {
		return true;
	}

	/**
	 * Sets the value of {@code hasPatches}. The implementor may do nothing, or reject the change if it has already been set.
	 * @param value The new value.
	 */
	public default void setHasPatches(boolean value) {}
}