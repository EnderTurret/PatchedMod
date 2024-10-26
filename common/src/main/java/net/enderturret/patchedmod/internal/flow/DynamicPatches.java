package net.enderturret.patchedmod.internal.flow;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.internal.PatchTargetManager;

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

	private static final Map<PackType, PatchTargetManager> PATCH_TARGET_MANAGERS = new EnumMap<>(PackType.class);

	static Map<PackResources, List<String>> getTargets(PackType type, ResourceLocation name, PackResources from) {
		final PatchTargetManager targetManager = PATCH_TARGET_MANAGERS.get(type);
		final Map<PackResources, List<String>> targets = targetManager == null ? Map.of() : targetManager.getTargets(name, from);

		if (DEBUG_TARGETS && !targets.isEmpty())
			Patched.platform().logger().info("Targets for {} (from {}): {}", name, from, targets);

		return targets;
	}

	/**
	 * Sets up the {@link PatchTargetManager} for the specified side using the given pack list.
	 * @param type The side to configure the target manager for.
	 * @param packsByPriority The list of packs, ordered by priority.
	 */
	@Internal
	public static void setupTargetManager(PackType type, List<PackResources> packsByPriority) {
		PATCH_TARGET_MANAGERS.put(type, new PatchTargetManager(type, packsByPriority));
	}

	/**
	 * Returns an unmodifiable view of the available target managers.
	 * @return The available target managers.
	 */
	@Internal
	public static Map<PackType, PatchTargetManager> getTargetManagers() {
		return Collections.unmodifiableMap(PATCH_TARGET_MANAGERS);
	}
}