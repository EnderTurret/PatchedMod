package net.enderturret.patchedmod.common.internal;

import static net.enderturret.patchedmod.common.internal.PatchedTestEvaluator.register;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.common.RootEvaluator;
import net.enderturret.patchedmod.common.TestCondition;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.flow.DynamicPatches;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

@Internal
final class PatchedTestConditions {

	static void registerDefaults() {
		register("patched:mod_loaded", (TestCondition.Simple) PatchedTestConditions::modLoaded);
		register("patched:registered", (TestCondition.Simple) PatchedTestConditions::registered);
		// Simpler version of "registered" specifically for items.
		register("patched:item_registered", (TestCondition.Simple) PatchedTestConditions::itemRegistered);
		register("patched:pack_enabled", PatchedTestConditions::packEnabled);
	}

	private static boolean modLoaded(JsonElement value) {
		if (value instanceof JsonObject obj) {
			final String modId = assertIsString("patched:mod_loaded", "mod", obj.get("mod"));
			final String version = assertIsString("patched:mod_loaded", "version", obj.get("version"));
			return PatchedPlatform.get().isModLoaded(modId, version);
		}

		return PatchedPlatform.get().isModLoaded(assertIsString("patched:mod_loaded", "value", value));
	}

	private static boolean registered(JsonElement value) {
		if (value instanceof JsonObject obj) {
			final PatchedResourceLocation registry = assertIsResourceLocation("patched:registered", "registry", obj.get("registry"));
			final PatchedResourceLocation id = assertIsResourceLocation("patched:registered", "id", obj.get("id"));
			return PatchedPlatform.get().isThingRegistered(registry, id);
		}

		throw new PatchingException("patched:registered: value must be an object, was \"" + value + "\"");
	}

	private static boolean itemRegistered(JsonElement value) {
		return PatchedPlatform.get().isItemRegistered(assertIsResourceLocation("patched:item_registered", "value", value));
	}

	private static boolean packEnabled(JsonElement root, JsonElement target, JsonElement value, PatchContext context) {
		final PatchedPackType type = ((RootEvaluator) context.testEvaluator()).packType();
		// Happens if someone uses PatchUtil.CONTEXT or INSTANCE directly (or otherwise constructs a type-agnostic evaluator).
		if (type == null) throw new PatchingException("Cannot use patched:pack_enabled in type-agnostic context");
		final PatchTargetManager manager = DynamicPatches.getTargetManagers().get(type);

		if (value instanceof JsonArray array) {
			if (array.isEmpty()) throw new PatchingException("patched:pack_enabled: value array must not be empty");

			for (int i = 0; i < array.size(); i++)
				if (manager.containsPack(assertIsString("patched:pack_enabled", "value$" + (i + 1), array.get(i))))
					return true;

			return false;
		}

		return manager.containsPack(assertIsString("patched:pack_enabled", "value", value));
	}

	/**
	 * If the given value is a {@link String}, returns it. Otherwise, throws an exception.
	 * @param name Some extra context for the message. Used to identify the test condition.
	 * @param field The name that the given value is associated with.
	 * @param value The given value.
	 * @return The given value as a {@link String}.
	 * @throws PatchingException
	 */
	private static String assertIsString(String name, String field, JsonElement value) throws PatchingException {
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
			throw new PatchingException(name + ": " + field + " must be a string, was \"" + value + "\"");

		return value.getAsString();
	}

	/**
	 * If the given value is a valid {@link PatchedResourceLocation}, returns it. Otherwise, throws an exception.
	 * @param name Some extra context for the message. Used to identify the test condition.
	 * @param field The name that the given value is associated with.
	 * @param value The given value.
	 * @return The given value as a {@link PatchedResourceLocation}.
	 * @throws PatchingException
	 */
	private static PatchedResourceLocation assertIsResourceLocation(String name, String field, JsonElement value) throws PatchingException {
		final String str = assertIsString(name, field, value);

		final PatchedResourceLocation loc = PatchedPlatform.get().tryParse(str);
		if (loc == null) throw new PatchingException(name + ": " + field + " must be a valid resource location, was \"" + value + "\"");

		return loc;
	}
}