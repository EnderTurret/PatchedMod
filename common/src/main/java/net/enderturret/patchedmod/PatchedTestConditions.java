package net.enderturret.patchedmod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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

	private static Map<String, ITestEvaluator> conditions = new ConcurrentHashMap<>();

	private PatchedTestConditions() {}

	@Internal
	public static void registerDefaults() {
		registerSimple(id("mod_loaded"),
				value -> {
					if (value instanceof JsonObject obj) {
						final String modId = PatchUtil.assertIsString("patched:mod_loaded", "mod", obj.get("mod"));
						final String version = PatchUtil.assertIsString("patched:mod_loaded", "version", obj.get("version"));
						return Patched.platform().isModLoaded(modId, version);
					}

					return Patched.platform().isModLoaded(PatchUtil.assertIsString("mod_loaded", "value", value));
				});

		registerSimple(id("registered"),
				value -> {
					if (value instanceof JsonObject obj) {
						final ResourceLocation registry = PatchUtil.assertIsResourceLocation("patched:registered", "registry", obj.get("registry"));
						final ResourceLocation id = PatchUtil.assertIsResourceLocation("patched:registered", "id", obj.get("id"));

						final Registry<?> reg = BuiltInRegistries.REGISTRY.get(registry);
						return reg != null && reg.containsKey(id);
					}

					throw new PatchingException("patched:registered: value must be an object, was \"" + value + "\"");
				});

		// Simpler version of "registered" specifically for items.
		registerSimple(id("item_registered"),
				value -> BuiltInRegistries.ITEM.containsKey(PatchUtil.assertIsResourceLocation("patched:item_registered", "value", value)));
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

	private static ResourceLocation id(String path) {
		return new ResourceLocation(Patched.MOD_ID, path);
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