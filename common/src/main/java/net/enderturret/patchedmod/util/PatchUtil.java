package net.enderturret.patchedmod.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;

import net.enderturret.patched.Patches;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;
import net.enderturret.patchedmod.mixin.FilePackResourcesAccess;

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

	public static List<ResourceLocation> getResources(PackResources pack, PackType type, String namespace, Predicate<ResourceLocation> filter) {
		if (pack instanceof FilePackResources fpp) return getFileResources(fpp, type, namespace, filter);

		final List<ResourceLocation> ret = new ArrayList<>();

		// This one's gonna require some explaining:
		// Basically, we want to look at all resources in the pack.
		// The problem is that Minecraft prevents this by bailing for paths "", ".", etc.
		//
		// So what we do here is swap the namespace and path so that it initially
		// resolves the same directory and then resolves the namespace directory.
		// We have to use a dot for the VanillaPackResources because otherwise LinkFileSystem's path handling gets a little concerned.
		try {
			final Function<ResourceLocation, ResourceLocation> renamer = Patched.platform().getRenamer(pack, namespace);
			final String fakeNamespace;
			final String fakePath;

			if (Patched.platform().needsSwapNamespaceAndPath(pack)) {
				// The vanilla pack throws on empty paths.
				fakeNamespace = pack instanceof VanillaPackResources ? "." : "";
				fakePath = namespace;
			} else {
				fakeNamespace = namespace;
				fakePath = "";
			}

			pack.listResources(type, fakeNamespace, fakePath, (loc, io) -> {
				if (filter.test(loc)) {
					final ResourceLocation renamed = renamer.apply(loc);

					ret.add(renamed);
				}
			});
		} catch (Exception e) {
			Patched.platform().logger().error("Exception listing resources:", e);
		}

		return ret;
	}

	/**
	 * This method is a better implementation of
	 * {@link FilePackResources#listResources(PackType, String, String, net.minecraft.server.packs.PackResources.ResourceOutput)}
	 * that actually works for what we need -- getting all resources under a particular namespace.
	 * @param pack The pack in question.
	 * @param type The pack type.
	 * @param namespace The namespace.
	 * @param filter A filter for deciding which resources to include in the returned list.
	 * @return The list of resources under the given namespace.
	 */
	private static List<ResourceLocation> getFileResources(FilePackResources pack, PackType type, String namespace, Predicate<ResourceLocation> filter) {
		final List<ResourceLocation> ret = new ArrayList<>();

		final ZipFile zip = ((FilePackResourcesAccess) pack).callGetOrCreateZipFile();
		if (zip == null) return ret;

		final String root = type.getDirectory() + "/" + namespace + "/";

		for (Enumeration<? extends ZipEntry> it = zip.entries(); it.hasMoreElements(); ) {
			final ZipEntry entry = it.nextElement();
			if (entry.isDirectory() || !entry.getName().startsWith(root)) continue;

			final String path = entry.getName().substring(root.length());
			final ResourceLocation loc = ResourceLocation.tryBuild(namespace, path);

			if (filter.test(loc))
				ret.add(loc);
		}

		return ret;
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