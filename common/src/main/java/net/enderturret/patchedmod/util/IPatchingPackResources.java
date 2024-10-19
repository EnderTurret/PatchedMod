package net.enderturret.patchedmod.util;

import net.minecraft.server.packs.PackResources;

import net.enderturret.patchedmod.internal.MixinCallbacks;
import net.enderturret.patchedmod.util.meta.PatchedMetadata;

/**
 * Provides access to {@link PatchedMetadata} in resource/data packs.
 * @author EnderTurret
 */
public interface IPatchingPackResources {

	/**
	 * Returns the {@link PatchedMetadata} associated with this pack.
	 * If necessary, it may be {@linkplain #initialized() initialized} first.
	 * @return The {@code PatchedMetadata} associated with this pack.
	 */
	public default PatchedMetadata patchedMetadata() {
		throw new UnsupportedOperationException("Method was not implemented");
	}

	/**
	 * Associates the specified {@code PatchedMetadata} with this pack, such that it can be retrieved via {@link #patchedMetadata()} (optional operation).
	 * @param value The new {@code PatchedMetadata}.
	 */
	public default void setPatchedMetadata(PatchedMetadata value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks if this pack has had its metadata initialized yet, and if not tries to initialize it.
	 * @return {@code true}.
	 */
	public default boolean checkInitialized() {
		if (!initialized())
			MixinCallbacks.maybeInitialize((PackResources) this);

		return true;
	}

	/**
	 * @return {@code true} if the Patched metadata has been initialized for this pack.
	 */
	public default boolean initialized() {
		throw new UnsupportedOperationException("Method was not implemented");
	}

	/**
	 * Whether the pack has any patches.
	 * This is an optimization for situations with a lot of mods and hardly any patches (if any).
	 * If necessary, the pack metadata may be {@linkplain #initialized() initialized} first.
	 * @deprecated Use {@link #patchedMetadata()} instead.
	 * @return {@code true} if this resource/data pack has patches.
	 */
	@Deprecated(since = "1.20.4")
	public default boolean hasPatches() {
		return patchedMetadata().patchingEnabled();
	}

	/**
	 * Sets the value of {@code hasPatches}.
	 * @deprecated Use {@link #setPatchedMetadata(PatchedMetadata)} instead.
	 * @implSpec Calls to this method may be ignored if the value has already been set or if the value is hard-coded.
	 * @param value The new value.
	 */
	@Deprecated(since = "1.20.4")
	public default void setHasPatches(boolean value) {
		setPatchedMetadata(PatchedMetadata.CURRENT_VERSION);
	}
}