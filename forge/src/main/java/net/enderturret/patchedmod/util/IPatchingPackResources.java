package net.enderturret.patchedmod.util;

/**
 * Represents Patched-specific metadata in resource/data packs.
 * @author EnderTurret
 */
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
	 * Sets the value of {@code hasPatches}.
	 * @implSpec Calls to this method may be ignored if the value has already been set or if the value is hard-coded.
	 * @param value The new value.
	 */
	public default void setHasPatches(boolean value) {}
}