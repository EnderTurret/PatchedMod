package net.enderturret.patchedmod.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patched.Patches;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;

/**
 * An assortment of utilities related to patching Json data.
 * @author EnderTurret
 */
public final class PatchUtil {

	/**
	 * The context used for patching Json. This context has all extensions enabled by default.
	 */
	public static final PatchContext CONTEXT = PatchContext.newContext().sbExtensions(true).patchedExtensions(true).testEvaluator(PatchedTestConditions.INSTANCE);

	/**
	 * The {@link Gson} instance used for reading patches and {@linkplain #readPrettyJson(InputStream, String, boolean, boolean) prettying Json data}.
	 */
	public static final Gson GSON = Patches.patchGson(CONTEXT.sbExtensions(), CONTEXT.patchedExtensions())
			.setPrettyPrinting().create();

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
	public static String readPrettyJson(InputStream is, String location, boolean requireJson, boolean logError) throws IOException {
		String ret = readString(is);

		try {
			final JsonElement elem = JsonParser.parseString(ret);
			ret = GSON.toJson(elem);
		} catch (Exception e) {
			if (logError)
				Patched.platform().logger().warn("Failed to parse {} as json:", location, e);

			if (requireJson)
				return null;

			// It's fine. Just use the normal version.
		}

		return ret;
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
	public static JsonElement readJson(InputStream is, String location, boolean logError) throws IOException {
		String ret = readString(is);

		try {
			return JsonParser.parseString(ret);
		} catch (Exception e) {
			if (logError)
				Patched.platform().logger().warn("Failed to parse {} as json:", location, e);
		}

		return null;
	}

	/**
	 * Reads the data in the given stream as a single string and returns it.
	 * @param is The stream to read from.
	 * @return The string.
	 * @throws IOException If an I/O-related error occurs.
	 */
	public static String readString(InputStream is) throws IOException {
		try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(isr)) {
			final StringBuilder sb = new StringBuilder();

			String line;
			while ((line = br.readLine()) != null) {
				if (sb.length() != 0)
					sb.append("\n");
				sb.append(line);
			}

			return sb.toString();
		}
	}

	/**
	 * If the given value is a {@link String}, returns it. Otherwise, throws an exception.
	 * @param name The name that the given value is associated with.
	 * @param value The given value.
	 * @return The given value as a {@link String}.
	 * @throws PatchingException
	 */
	public static String assertIsString(String name, JsonElement value) throws PatchingException {
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
			throw new PatchingException(name + ": value must be a string, was \"" + value + "\"");

		return value.getAsString();
	}

	/**
	 * @param location The location of the file to test.
	 * @return {@code true} if the file at the given location supports being patched, based on its name.
	 */
	public static boolean isPatchable(ResourceLocation location) {
		final String path = location.getPath();
		return path.endsWith(".json") || (path.endsWith(".mcmeta") && !path.equals("pack.mcmeta"));
	}
}