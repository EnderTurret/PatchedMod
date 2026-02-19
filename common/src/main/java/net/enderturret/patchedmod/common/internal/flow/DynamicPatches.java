package net.enderturret.patchedmod.common.internal.flow;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.PatchTargetManager;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Manages the "dynamic patch" subsystem of Patched.
 * @author EnderTurret
 */
@Internal
public final class DynamicPatches {

	/**
	 * Allows turning on debug messages about patch targets.
	 * These are extremely spammy, and so are off by default.
	 */
	@Internal
	public static final boolean DEBUG_TARGETS = Boolean.getBoolean("patched.debugTargets");

	private static final Map<PatchedPackType, PatchTargetManager> PATCH_TARGET_MANAGERS = new EnumMap<>(PatchedPackType.class);

	public static Map<PatchedPackResources, List<String>> getTargets(PatchedPackType type, PatchedResourceLocation name, PatchedPackResources from) {
		final PatchTargetManager targetManager = PATCH_TARGET_MANAGERS.get(type);
		final Map<PatchedPackResources, List<String>> targets = targetManager == null ? Map.of() : targetManager.getTargets(name, from);

		if (DEBUG_TARGETS && !targets.isEmpty())
			PatchedInternal.LOGGER.info("Targets for {} (from {}): {}", name, from, targets);

		return targets;
	}

	/**
	 * Sets up the {@link PatchTargetManager} for the specified side using the given pack list.
	 * @param type The side to configure the target manager for.
	 * @param packsByPriority The list of packs, ordered by priority.
	 */
	@Internal
	public static void setupTargetManager(PatchedPackType type, List<PatchedPackResources> packsByPriority) {
		PATCH_TARGET_MANAGERS.put(type, new PatchTargetManager(type, packsByPriority));
	}

	/**
	 * Returns an unmodifiable view of the available target managers.
	 * @return The available target managers.
	 */
	@Internal
	public static Map<PatchedPackType, PatchTargetManager> getTargetManagers() {
		return Collections.unmodifiableMap(PATCH_TARGET_MANAGERS);
	}
}