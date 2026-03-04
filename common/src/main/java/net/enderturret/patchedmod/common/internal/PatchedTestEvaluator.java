package net.enderturret.patchedmod.common.internal;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.context.PatchContext;
import net.enderturret.patchedmod.common.RootEvaluator;
import net.enderturret.patchedmod.common.TestCondition;
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
		PatchedTestConditions.registerDefaults();
	}
}