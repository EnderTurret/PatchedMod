package net.enderturret.patchedmod.common.util;

import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;
import net.enderturret.patchedmod.internal.flow.PatchingManager;

/**
 * Provides access to {@link PatchedMetadata} in resource/data packs.
 * @author EnderTurret
 */
// TODO 1.20.5: Prefix initialized() and checkInitialized() so there's absolutely no chances of mixin conflicts.
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
			PatchingManager.maybeInitialize(this);

		return true;
	}

	/**
	 * @return {@code true} if the Patched metadata has been initialized for this pack.
	 */
	public default boolean initialized() {
		throw new UnsupportedOperationException("Method was not implemented");
	}
}