package net.enderturret.patchedmod.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.internal.PatchedInternal;

/**
 * An assortment of utilities related to patching Json data.
 * @author EnderTurret
 */
public final class PatchUtil {

	/**
	 * The context used for patching Json. This context has all extensions enabled by default.
	 */
	@Deprecated(since = "7.3.0+1.21.1")
	public static final PatchContext CONTEXT = PatchedInternal.BASE_CONTEXT;

	/**
	 * The {@link Gson} instance used for reading patches and {@linkplain #readPrettyJson(InputStream, String, boolean, boolean) prettying Json data}.
	 */
	@Deprecated(since = "7.3.0+1.21.1")
	public static final Gson GSON = PatchedInternal.GSON;

	/**
	 * Returns a list of all resources in the provided pack under the given namespace that match the specified filter.
	 * @deprecated
	 * @param pack The pack to look for resources in.
	 * @param type The type of resources to look for. Most important for mods, which may have both kinds.
	 * @param namespace The namespace to look under.
	 * @param filter A filter for filtering out undesired results.
	 * @return The list of resources.
	 */
	@Deprecated(since = "7.3.0+1.21.1")
	public static List<ResourceLocation> getResources(PackResources pack, PackType type, String namespace, Predicate<ResourceLocation> filter) {
		return PatchedInternal.getResources(pack, type, namespace, filter);
	}

	/**
	 * Attempts to read a string from the given stream as Json, converted to a "pretty" form.
	 * @param is The stream to read from.
	 * @param location The location of the file the stream is from. Used for error handling.
	 * @param requireJson Whether to require the data to be valid Json. {@code false} allows this method to fallback to the original string if an error occurs.
	 * @param logError Whether to log a warning if the data is not valid Json.
	 * @return The "pretty-printed" form of the Json in the given stream.
	 * @throws IOException If an I/O-related error occurs when reading the string from the stream.
	 */
	@Nullable
	@Deprecated(since = "7.3.0+1.21.1")
	public static String readPrettyJson(InputStream is, String location, boolean requireJson, boolean logError) throws IOException {
		return PatchedInternal.readPrettyJson(is, location, requireJson, logError);
	}

	/**
	 * Attempts to read a string from the given stream as Json.
	 * @param is The stream to read from.
	 * @param location The location of the file the stream is from. Used for error handling.
	 * @param logError Whether to log a warning if the data is not valid Json.
	 * @return The Json in the given stream.
	 * @throws IOException If an I/O-related error occurs when reading the string from the stream.
	 */
	@Nullable
	@Deprecated(since = "7.3.0+1.21.1")
	public static JsonElement readJson(InputStream is, String location, boolean logError) throws IOException {
		return PatchedInternal.readJson(is, location, logError);
	}

	/**
	 * Reads the data in the given stream as a single string and returns it.
	 * @param is The stream to read from.
	 * @return The string.
	 * @throws IOException If an I/O-related error occurs.
	 */
	@Deprecated(since = "7.3.0+1.21.1")
	public static String readString(InputStream is) throws IOException {
		return PatchedInternal.readString(is);
	}

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
	 * Simplified version of {@link #assertIsString(String, String, JsonElement)}.
	 * @deprecated Use {@link #assertIsString(String, String, JsonElement) assertIsString(String, "value", JsonElement)} instead.
	 * @param name Some extra context for the message. Used to identify the test condition.
	 * @param value The given value.
	 * @return The given value as a {@link String}.
	 * @throws PatchingException
	 */
	@Deprecated(since = "1.20.4")
	public static String assertIsString(String name, JsonElement value) throws PatchingException {
		return assertIsString(name, "value", value);
	}

	/**
	 * If the given value is a valid {@link ResourceLocation}, returns it. Otherwise, throws an exception.
	 * @param name Some extra context for the message. Used to identify the test condition.
	 * @param field The name that the given value is associated with.
	 * @param value The given value.
	 * @return The given value as a {@link ResourceLocation}.
	 * @throws PatchingException
	 */
	public static ResourceLocation assertIsResourceLocation(String name, String field, JsonElement value) throws PatchingException {
		final String str = assertIsString(name, field, value);

		final ResourceLocation loc = ResourceLocation.tryParse(str);
		if (loc == null) throw new PatchingException(name + ": " + field + " must be a valid resource location, was \"" + value + "\"");

		return loc;
	}

	/**
	 * @param location The location of the file to test.
	 * @return {@code true} if the file at the given location supports being patched, based on its name.
	 */
	public static boolean isPatchable(ResourceLocation location) {
		final String path = location.getPath();
		return path.endsWith(".json") || path.endsWith(".json.patch") || (path.endsWith(".mcmeta") && !path.equals("pack.mcmeta"));
	}
}