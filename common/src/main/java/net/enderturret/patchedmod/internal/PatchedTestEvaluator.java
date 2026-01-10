package net.enderturret.patchedmod.internal;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.RootEvaluator;
import net.enderturret.patchedmod.TestCondition;
import net.enderturret.patchedmod.TestCondition.Simple;
import net.enderturret.patchedmod.internal.flow.DynamicPatches;
import net.enderturret.patchedmod.util.PatchUtil;

/**
 * Patched's implementation of {@link ITestEvaluator}.
 * Mods can register their own test conditions using {@link Patched#registerTestCondition(Identifier, TestCondition)}
 * and {@link Patched#registerSimpleTestCondition(Identifier, Simple)}.
 * @author EnderTurret
 */
@Internal
public final class PatchedTestEvaluator implements RootEvaluator {

	private static final Map<String, TestCondition> CONDITIONS = new ConcurrentHashMap<>();

	@Nullable
	private final PackType type;

	/**
	 * Constructs a new {@code PatchedTestEvaluator}.
	 * @param type The pack type.
	 */
	@Internal
	public PatchedTestEvaluator(@Nullable PackType type) {
		this.type = type;
	}

	@Override
	@Nullable
	public PackType packType() {
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
	public static void register(Identifier key, TestCondition value) {
		CONDITIONS.put(key.toString(), Objects.requireNonNull(value, "value"));
	}

	/**
	 * Internal test condition registration method for legacy conditions.
	 * @param key The name of the test condition -- what goes in the {@code type} field.
	 * @param value The condition to register.
	 */
	@Internal
	public static void registerLegacy(Identifier key, ITestEvaluator value) {
		Objects.requireNonNull(value);
		final String str = key.toString();
		register(key, (root, target, _value, context) -> value.test(root, str, target, _value, context));
	}

	/**
	 * Registers all default test conditions.
	 */
	@Internal
	public static void registerDefaults() {
		Patched.registerSimpleTestCondition(PatchedVersionUtil.id("mod_loaded"), PatchedTestEvaluator::modLoaded);
		Patched.registerSimpleTestCondition(PatchedVersionUtil.id("registered"), PatchedTestEvaluator::registered);
		// Simpler version of "registered" specifically for items.
		Patched.registerSimpleTestCondition(PatchedVersionUtil.id("item_registered"), PatchedTestEvaluator::itemRegistered);
		Patched.registerTestCondition(PatchedVersionUtil.id("pack_enabled"), PatchedTestEvaluator::packEnabled);
	}

	private static boolean modLoaded(JsonElement value) {
		if (value instanceof JsonObject obj) {
			final String modId = PatchUtil.assertIsString("patched:mod_loaded", "mod", obj.get("mod"));
			final String version = PatchUtil.assertIsString("patched:mod_loaded", "version", obj.get("version"));
			return Patched.platform().isModLoaded(modId, version);
		}

		return Patched.platform().isModLoaded(PatchUtil.assertIsString("patched:mod_loaded", "value", value));
	}

	private static boolean registered(JsonElement value) {
		if (value instanceof JsonObject obj) {
			final Identifier registry = PatchUtil.assertIsResourceLocation("patched:registered", "registry", obj.get("registry"));
			final Identifier id = PatchUtil.assertIsResourceLocation("patched:registered", "id", obj.get("id"));

			final Registry<?> reg = PatchedVersionUtil.get(BuiltInRegistries.REGISTRY, registry);
			return reg != null && reg.containsKey(id);
		}

		throw new PatchingException("patched:registered: value must be an object, was \"" + value + "\"");
	}

	private static boolean itemRegistered(JsonElement value) {
		return BuiltInRegistries.ITEM.containsKey(PatchUtil.assertIsResourceLocation("patched:item_registered", "value", value));
	}

	private static boolean packEnabled(JsonElement root, JsonElement target, JsonElement value, PatchContext context) {
		final PackType type = ((RootEvaluator) context.testEvaluator()).packType();
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