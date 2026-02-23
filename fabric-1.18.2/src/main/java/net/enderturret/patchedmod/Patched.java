package net.enderturret.patchedmod;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patched.IDataSource;
import net.enderturret.patchedmod.common.SingleDataSource;
import net.enderturret.patchedmod.common.TestCondition;
import net.enderturret.patchedmod.common.internal.PatchedDataSource;
import net.enderturret.patchedmod.common.internal.PatchedTestEvaluator;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;

/**
 * Patched's loader-agnostic API.
 * @author EnderTurret
 * @see #registerDataSource(ResourceLocation, SingleDataSource)
 * @see #registerTestCondition(ResourceLocation, TestCondition)
 * @see #registerSimpleTestCondition(ResourceLocation, TestCondition.Simple)
 */
public final class Patched {

	/**
	 * Patched's mod ID.
	 */
	public static final String MOD_ID = PatchedPlatform.MOD_ID;

	private Patched() {}

	/**
	 * Registers a new {@linkplain IDataSource data source}.
	 * @param id The name of the data source -- what goes in the {@code type} field.
	 * @param source The data source to register.
	 */
	public static void registerDataSource(ResourceLocation id, SingleDataSource source) {
		PatchedDataSource.register(id.toString(), source);
	}

	/**
	 * Registers a new {@linkplain TestCondition test condition}.
	 * @param id The name of the test condition -- what goes in the {@code type} field.
	 * @param condition The condition to register.
	 */
	public static void registerTestCondition(ResourceLocation id, TestCondition condition) {
		PatchedTestEvaluator.register(id.toString(), condition);
	}

	/**
	 * Registers a new {@linkplain net.enderturret.patchedmod.common.TestCondition.Simple simple test condition}.
	 * @param id The name of the test condition -- what goes in the {@code type} field.
	 * @param condition The condition to register.
	 */
	public static void registerSimpleTestCondition(ResourceLocation id, TestCondition.Simple condition) {
		registerTestCondition(id, condition);
	}
}