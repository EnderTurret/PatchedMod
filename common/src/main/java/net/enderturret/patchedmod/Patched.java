package net.enderturret.patchedmod;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patchedmod.internal.PatchedTestEvaluator;
import net.enderturret.patchedmod.internal.env.DummyPlatform;
import net.enderturret.patchedmod.util.PatchUtil;
import net.enderturret.patchedmod.util.env.IPlatform;

/**
 * Patched's loader-agnostic entrypoint and API.
 * @author EnderTurret
 * @see #registerTestCondition(ResourceLocation, TestCondition)
 * @see #registerSimpleTestCondition(ResourceLocation, TestCondition.Simple)
 */
public final class Patched {

	/**
	 * Patched's mod ID.
	 */
	public static final String MOD_ID = "patched";

	private static IPlatform platform;

	private Patched() {}

	/**
	 * Returns Patched's platform instance.
	 * @return The platform.
	 */
	@Internal
	public static IPlatform platform() {
		if (platform == null) {
			platform = new DummyPlatform();
			platform.logger().error("Constructed dummy platform instance! If you're reading this, Patched was not loaded correctly!");
			// Or someone called this method way too early, but no one would do that, right?
		}

		return platform;
	}

	/**
	 * Sets Patched's platform instance.
	 * @param value The new value.
	 */
	@Internal
	public static void setPlatform(IPlatform value) {
		platform = Objects.requireNonNull(value);
		PatchedTestEvaluator.registerDefaults();
	}

	/**
	 * Registers a new {@linkplain TestCondition test condition}.
	 * @param id The name of the test condition -- what goes in the {@code type} field.
	 * @param condition The condition to register.
	 */
	public static void registerTestCondition(ResourceLocation id, TestCondition condition) {
		PatchedTestEvaluator.register(id, condition);
	}

	/**
	 * Registers a new {@linkplain TestCondition.Simple simple test condition}.
	 * @param id The name of the test condition -- what goes in the {@code type} field.
	 * @param condition The condition to register.
	 */
	public static void registerSimpleTestCondition(ResourceLocation id, TestCondition.Simple condition) {
		PatchedTestEvaluator.register(id, condition);
	}

	/**
	 * @deprecated Use {@link PatchUtil#isPatchable(ResourceLocation)} instead.
	 * @param location The location of the file to test.
	 * @return {@code true} if the file at the given location supports being patched, based on its name.
	 */
	@Deprecated(forRemoval = true)
	public static boolean canBePatched(ResourceLocation location) {
		return PatchUtil.isPatchable(location);
	}
}