package net.enderturret.patchedmod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.util.PatchUtil;

/**
 * Handles evaluating custom test conditions.
 * @author EnderTurret
 */
public final class PatchedTestConditions implements ITestEvaluator {

	public static final PatchedTestConditions INSTANCE = new PatchedTestConditions();

	private static Map<String, ITestEvaluator> conditions = new HashMap<>();

	private PatchedTestConditions() {}

	@Internal
	public static void registerDefaults() {
		PatchedTestConditions.registerSimple(new ResourceLocation(Patched.MOD_ID, "mod_loaded"),
				value -> Patched.platform().isModLoaded(PatchUtil.assertIsString("mod_loaded", value)));
	}

	/**
	 * Registers the given condition under the given name.
	 * @param name The name of the condition. This will be the {@code type} value that the condition is invoked for.
	 * @param condition The condition itself.
	 */
	public static void register(ResourceLocation name, ITestEvaluator condition) {
		conditions.put(name.toString(), Objects.requireNonNull(condition));
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