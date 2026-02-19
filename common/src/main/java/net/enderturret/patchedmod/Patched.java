package net.enderturret.patchedmod;

import net.minecraft.resources.Identifier;

import net.enderturret.patched.IDataSource;
import net.enderturret.patchedmod.common.SingleDataSource;
import net.enderturret.patchedmod.common.TestCondition;
import net.enderturret.patchedmod.common.internal.PatchedDataSource;
import net.enderturret.patchedmod.common.internal.PatchedTestEvaluator;

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

	private Patched() {}

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
		PatchedTestEvaluator.register(id.toString(), condition);
	}

	/**
	 * Registers a new {@linkplain net.enderturret.patchedmod.common.TestCondition.Simple simple test condition}.
	 * @param id The name of the test condition -- what goes in the {@code type} field.
	 * @param condition The condition to register.
	 */
	public static void registerSimpleTestCondition(Identifier id, TestCondition.Simple condition) {
		registerTestCondition(id, condition);
	}
}