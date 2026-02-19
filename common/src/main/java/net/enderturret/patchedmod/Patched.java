package net.enderturret.patchedmod;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.Identifier;

import net.enderturret.patched.IDataSource;
import net.enderturret.patchedmod.common.SingleDataSource;
import net.enderturret.patchedmod.common.TestCondition;
import net.enderturret.patchedmod.common.internal.PatchedDataSource;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.IPlatform;
import net.enderturret.patchedmod.internal.PatchedTestEvaluator;
import net.enderturret.patchedmod.internal.env.DummyPlatform;

/**
 * Patched's loader-agnostic entrypoint and API.
 * @author EnderTurret
 * @see #registerDataSource(Identifier, SingleDataSource)
 * @see #registerTestCondition(Identifier, TestCondition)
 * @see #registerSimpleTestCondition(Identifier, TestCondition.Simple)
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
			PatchedInternal.LOGGER.error("Constructed dummy platform instance! If you're reading this, Patched was not loaded correctly!");
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
	 * Registers a new {@linkplain IDataSource data source}.
	 * @param id The name of the data source -- what goes in the {@code type} field.
	 * @param source The data source to register.
	 */
	public static void registerDataSource(Identifier id, SingleDataSource source) {
		PatchedDataSource.register(id.toString(), source);
	}

	/**
	 * Registers a new {@linkplain TestCondition test condition}.
	 * @param id The name of the test condition -- what goes in the {@code type} field.
	 * @param condition The condition to register.
	 */
	public static void registerTestCondition(Identifier id, TestCondition condition) {
		PatchedTestEvaluator.register(id, condition);
	}

	/**
	 * Registers a new {@linkplain net.enderturret.patchedmod.common.TestCondition.Simple simple test condition}.
	 * @param id The name of the test condition -- what goes in the {@code type} field.
	 * @param condition The condition to register.
	 */
	public static void registerSimpleTestCondition(Identifier id, TestCondition.Simple condition) {
		PatchedTestEvaluator.register(id, condition);
	}
}