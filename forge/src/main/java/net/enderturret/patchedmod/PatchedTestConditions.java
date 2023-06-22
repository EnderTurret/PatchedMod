package net.enderturret.patchedmod;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;

/**
 * Handles evaluating custom test conditions.
 * @author EnderTurret
 */
public final class PatchedTestConditions implements ITestEvaluator {

	public static final PatchedTestConditions INSTANCE = new PatchedTestConditions();

	private static Map<String, ITestEvaluator> conditions = new HashMap<>();

	private PatchedTestConditions() {}

	/**
	 * Registers the given condition under the given name.
	 * @param name The name of the condition. This will be the {@code type} value that the condition is invoked for.
	 * @param condition The condition itself.
	 */
	public static void register(ResourceLocation name, ITestEvaluator condition) {
		if (condition == null) throw new NullPointerException();
		conditions.put(name.toString(), condition);
	}

	/**
	 * Registers the given condition under the given name.
	 * This is a "simpler" version of {@link #register(ResourceLocation, ITestEvaluator)} that is much more lambda-friendly.
	 * @param name The name of the condition. This will be the {@code type} value that the condition is invoked for.
	 * @param condition The condition itself.
	 */
	public static void registerSimple(ResourceLocation name, ISimpleTestEvaluator condition) {
		register(name, condition);
	}

	@Override
	public boolean test(JsonElement root, String type, JsonElement target, JsonElement value, PatchContext context) {
		final ITestEvaluator con = conditions.get(type);
		return con != null && con.test(root, type, target, value, context);
	}

	/**
	 * Represents a "simple" condition.
	 * See {@link PatchedTestConditions#registerSimple(ResourceLocation, ISimpleTestEvaluator)} for more information.
	 * @author EnderTurret
	 */
	@FunctionalInterface
	public static interface ISimpleTestEvaluator extends ITestEvaluator {

		public boolean test(JsonElement value);

		@Override
		default boolean test(JsonElement root, String type, JsonElement target, JsonElement value, PatchContext context) {
			if (value == null)
				throw new PatchingException(type + ": value must not be null");

			return test(value);
		}
	}
}