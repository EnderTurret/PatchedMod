package net.enderturret.patchedmod.common.env;

import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;
import net.enderturret.patchedmod.internal.flow.PatchingManager;

/**
 * Provides access to {@link PatchedMetadata} in resource/data packs.
 * @author EnderTurret
 */
public interface IPatchingPackResources {

	/**
	 * Returns the {@link PatchedMetadata} associated with this pack.
	 * If necessary, it may be {@linkplain #patched$initialized() initialized} first.
	 * @return The {@code PatchedMetadata} associated with this pack.
	 */
	public default PatchedMetadata patchedMetadata() {
		return PatchedMetadata.DISABLED_METADATA;
	}

	/**
	 * Associates the specified {@code PatchedMetadata} with this pack, such that it can be retrieved via {@link #patchedMetadata()} (optional operation).
	 * @param value The new {@code PatchedMetadata}.
	 */
	public default void setPatchedMetadata(PatchedMetadata value) {
		throw new UnsupportedOperationException();
	}

	public default boolean patched$hasPatches() {
		return patchedMetadata().patchingEnabled();
	}

	/**
	 * Checks if this pack has had its metadata initialized yet, and if not tries to initialize it.
	 * @return {@code true}.
	 */
	public default boolean patched$checkInitialized() {
		if (!patched$initialized())
			PatchingManager.maybeInitialize(this);

		return true;
	}

	/**
	 * @return {@code true} if the Patched metadata has been initialized for this pack.
	 */
	public default boolean patched$initialized() {
		return true;
	}
}