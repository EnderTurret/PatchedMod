package net.enderturret.patchedmod.common.util;

import com.google.gson.JsonElement;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;

/**
 * An assortment of utilities related to patching Json data.
 * @author EnderTurret
 */
public final class PatchUtil {

	/**
	 * If the given value is a {@link String}, returns it. Otherwise, throws an exception.
	 * @param name Some extra context for the message. Used to identify the test condition.
	 * @param field The name that the given value is associated with.
	 * @param value The given value.
	 * @return The given value as a {@link String}.
	 * @throws PatchingException
	 */
	public static String assertIsString(String name, String field, JsonElement value) throws PatchingException {
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
	public static PatchedResourceLocation assertIsResourceLocation(String name, String field, JsonElement value) throws PatchingException {
		final String str = assertIsString(name, field, value);

		final PatchedResourceLocation loc = PatchedPlatform.get().tryParse(str);
		if (loc == null) throw new PatchingException(name + ": " + field + " must be a valid resource location, was \"" + value + "\"");

		return loc;
	}

	/**
	 * @param path The location of the file to test.
	 * @return {@code true} if the file at the given location supports being patched, based on its name.
	 */
	public static boolean isPatchable(String path) {
		return path.endsWith(".json") || path.endsWith(".json.patch") || (path.endsWith(".mcmeta") && !path.equals("pack.mcmeta"));
	}
}