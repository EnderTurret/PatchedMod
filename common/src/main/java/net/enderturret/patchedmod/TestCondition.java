package net.enderturret.patchedmod;

import com.google.gson.JsonElement;

import net.minecraft.resources.Identifier;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.patch.PatchContext;

/**
 * A version of {@link ITestEvaluator} without the {@code type} argument.
 * @author EnderTurret
 * @see Patched#registerTestCondition(Identifier, TestCondition)
 * @see Patched#registerSimpleTestCondition(Identifier, Simple)
 * @see Simple
 */
public interface TestCondition {

	/**
	 * Determines whether the condition succeeds for the specified parameters.
	 * @param root The root element.
	 * @param target The target element. In a normal {@code test} patch, this is the element tested for equality. May be {@code null}.
	 * @param value The value. In a normal {@code test} patch, this is the element that {@code target} is being compared against. May be {@code null}.
	 * @param context The patch context.
	 * @return {@code true} if the test succeeds, {@code false} otherwise.
	 */
	public boolean test(JsonElement root, JsonElement target, JsonElement value, PatchContext context);

	/**
	 * Simplified version of {@link TestCondition} that only uses the {@code value} argument.
	 * Suitable for most test conditions.
	 * @author EnderTurret
	 * @see Patched#registerSimpleTestCondition(Identifier, Simple)
	 */
	public static interface Simple extends TestCondition {

		/**
		 * Determines whether the condition succeeds for the specified value.
		 * This is a simplified version of {@link #test(JsonElement, JsonElement, JsonElement, PatchContext)}.
		 * @param value The value given by the patch.
		 * @return {@code true} if the condition succeeds, {@code false} otherwise.
		 */
		public boolean test(JsonElement value);

		@Override
		public default boolean test(JsonElement root, JsonElement target, JsonElement value, PatchContext context) {
			return test(value);
		}
	}
}