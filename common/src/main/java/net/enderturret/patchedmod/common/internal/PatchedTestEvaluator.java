package net.enderturret.patchedmod.common.internal;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.common.RootEvaluator;
import net.enderturret.patchedmod.common.TestCondition;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.internal.flow.DynamicPatches;
import net.enderturret.patchedmod.common.util.PatchUtil;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Patched's implementation of {@link ITestEvaluator}.
 * @author EnderTurret
 */
@Internal
public final class PatchedTestEvaluator implements RootEvaluator {

	private static final Map<String, TestCondition> CONDITIONS = new ConcurrentHashMap<>();

	@Nullable
	private final PatchedPackType type;

	/**
	 * Constructs a new {@code PatchedTestEvaluator}.
	 * @param type The pack type.
	 */
	@Internal
	public PatchedTestEvaluator(@Nullable PatchedPackType type) {
		this.type = type;
	}

	@Override
	@Nullable
	public PatchedPackType packType() {
		return type;
	}

	@Override
	public boolean test(JsonElement root, String type, @Nullable JsonElement target, @Nullable JsonElement value, PatchContext context) {
		final TestCondition con = CONDITIONS.get(type);
		if (con != null) return con.test(root, target, value, context);
		throw new PatchingException("Unknown test type: '" + type + "'");
	}

	/**
	 * Internal test condition registration method.
	 * @param key The name of the test condition -- what goes in the {@code type} field.
	 * @param value The condition to register.
	 */
	@Internal
	public static void register(String key, TestCondition value) {
		CONDITIONS.put(key, Objects.requireNonNull(value, "value"));
	}

	/**
	 * Internal test condition registration method for legacy conditions.
	 * @param key The name of the test condition -- what goes in the {@code type} field.
	 * @param value The condition to register.
	 */
	@Internal
	public static void registerLegacy(String key, ITestEvaluator value) {
		Objects.requireNonNull(value);
		register(key, (root, target, _value, context) -> value.test(root, key, target, _value, context));
	}

	/**
	 * Registers all default test conditions.
	 */
	@Internal
	public static void registerDefaults() {
		register("patched:mod_loaded", (TestCondition.Simple) PatchedTestEvaluator::modLoaded);
		register("patched:registered", (TestCondition.Simple) PatchedTestEvaluator::registered);
		// Simpler version of "registered" specifically for items.
		register("patched:item_registered", (TestCondition.Simple) PatchedTestEvaluator::itemRegistered);
		register("patched:pack_enabled", PatchedTestEvaluator::packEnabled);
	}

	private static boolean modLoaded(JsonElement value) {
		if (value instanceof JsonObject obj) {
			final String modId = PatchUtil.assertIsString("patched:mod_loaded", "mod", obj.get("mod"));
			final String version = PatchUtil.assertIsString("patched:mod_loaded", "version", obj.get("version"));
			return PatchedPlatform.get().isModLoaded(modId, version);
		}

		return PatchedPlatform.get().isModLoaded(PatchUtil.assertIsString("patched:mod_loaded", "value", value));
	}

	private static boolean registered(JsonElement value) {
		if (value instanceof JsonObject obj) {
			final PatchedResourceLocation registry = PatchUtil.assertIsResourceLocation("patched:registered", "registry", obj.get("registry"));
			final PatchedResourceLocation id = PatchUtil.assertIsResourceLocation("patched:registered", "id", obj.get("id"));
			return PatchedPlatform.get().isThingRegistered(registry, id);
		}

		throw new PatchingException("patched:registered: value must be an object, was \"" + value + "\"");
	}

	private static boolean itemRegistered(JsonElement value) {
		return PatchedPlatform.get().isItemRegistered(PatchUtil.assertIsResourceLocation("patched:item_registered", "value", value));
	}

	private static boolean packEnabled(JsonElement root, JsonElement target, JsonElement value, PatchContext context) {
		final PatchedPackType type = ((RootEvaluator) context.testEvaluator()).packType();
		// Happens if someone uses PatchUtil.CONTEXT or INSTANCE directly (or otherwise constructs a type-agnostic evaluator).
		if (type == null) throw new PatchingException("Cannot use patched:pack_enabled in type-agnostic context");
		final PatchTargetManager manager = DynamicPatches.getTargetManagers().get(type);

		if (value instanceof JsonArray array) {
			if (array.isEmpty()) throw new PatchingException("patched:pack_enabled: value array must not be empty");

			for (int i = 0; i < array.size(); i++)
				if (manager.containsPack(PatchUtil.assertIsString("patched:pack_enabled", "value$" + (i + 1), array.get(i))))
					return true;

			return false;
		}

		return manager.containsPack(PatchUtil.assertIsString("patched:pack_enabled", "value", value));
	}
}