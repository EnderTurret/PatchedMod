package net.enderturret.patchedmod;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.internal.PatchedTestEvaluator;

/**
 * Handles evaluating custom test conditions.
 * @deprecated Use {@link Patched#registerTestCondition(ResourceLocation, TestCondition)} instead.
 * @author EnderTurret
 */
@Deprecated(since = "7.3.0+1.21.1")
public final class PatchedTestConditions implements RootEvaluator {

	/**
	 * The singleton instance of {@code PatchedTestConditions}.
	 * @deprecated Use {@link #getRootEvaluator(PackType)} instead.
	 */
	@Deprecated
	public static final PatchedTestConditions INSTANCE = new PatchedTestConditions(null);

	private final PackType type;

	private PatchedTestConditions(@Nullable PackType type) {
		this.type = type;
	}

	@Override
	@Nullable
	public PackType packType() {
		return type;
	}

	/**
	 * Returns the {@linkplain ITestEvaluator custom test evaluator} for the specified {@code PackType}.
	 * A {@code null} {@code PackType} indicates this method should return a type-agnostic test evaluator (turns off some test types).
	 * @deprecated
	 * @param type The type to create the evaluator for. This customizes some test types that require knowledge of the {@code PackType}. Passing {@code null} turns them off.
	 * @return The test evaluator.
	 */
	@Deprecated(since = "7.3.0+1.21.1")
	public static RootEvaluator getRootEvaluator(@Nullable PackType type) {
		return new PatchedTestConditions(type);
	}

	/**
	 * Registers the given condition under the given name.
	 * @deprecated Use {@link Patched#registerTestCondition(ResourceLocation, TestCondition)} instead.
	 * @param name The name of the condition. This will be the {@code type} value that the condition is invoked for.
	 * @param condition The condition itself.
	 */
	@Deprecated(since = "7.3.0+1.21.1")
	public static void register(ResourceLocation name, ITestEvaluator condition) {
		PatchedTestEvaluator.registerLegacy(name, condition);
	}

	/**
	 * Registers the given condition under the given name.
	 * This is a "simpler" version of {@link #register(ResourceLocation, ITestEvaluator)} that is much more lambda-friendly.
	 * @deprecated Use {@link Patched#registerSimpleTestCondition(ResourceLocation, TestCondition.Simple)} instead.
	 * @param name The name of the condition. This will be the {@code type} value that the condition is invoked for.
	 * @param condition The condition itself.
	 */
	@Deprecated(since = "7.3.0+1.21.1")
	public static void registerSimple(ResourceLocation name, ISimpleTestEvaluator condition) {
		register(name, condition);
	}

	@Override
	public boolean test(JsonElement root, String type, JsonElement target, JsonElement value, PatchContext context) {
		return false;
	}

	/**
	 * Represents a "simple" condition.
	 * See {@link PatchedTestConditions#registerSimple(ResourceLocation, ISimpleTestEvaluator)} for more information.
	 * @author EnderTurret
	 */
	@FunctionalInterface
	@Deprecated(since = "7.3.0+1.21.1")
	public static interface ISimpleTestEvaluator extends ITestEvaluator {

		/**
		 * Determines whether the condition succeeds for the specified value.
		 * This is a simplified version of {@link #test(JsonElement, String, JsonElement, JsonElement, PatchContext)}.
		 * @param value The value given by the patch.
		 * @return {@code true} if the condition succeeds, {@code false} otherwise.
		 */
		public boolean test(JsonElement value);

		@Override
		public default boolean test(JsonElement root, String type, @Nullable JsonElement target, @Nullable JsonElement value, PatchContext context) {
			if (value == null)
				throw new PatchingException(type + ": value must not be null");

			return test(value);
		}
	}
}