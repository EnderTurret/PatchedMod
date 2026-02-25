package net.enderturret.patchedmod.common.internal;

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

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.enderturret.patched.Patches;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Internal utilities for Patched.
 * @author EnderTurret
 */
@Internal
public final class PatchedInternal {

	/**
	 * Patched's logger instance.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger("Patched");

	/**
	 * Patched's platform instance.
	 * Prefer getting this via {@link PatchedPlatform#get()}.
	 */
	public static PatchedPlatform platform;

	/**
	 * The context used for patching json. This context has all extensions enabled by default.
	 */
	@Internal
	public static final PatchContext BASE_CONTEXT = PatchContext.newContext()
			.testExtensions(true)
			.patchedExtensions(true)
			.testEvaluator(new PatchedTestEvaluator(null))
			.dataSource(new PatchedDataSource());

	/**
	 * The {@link Gson} instance used for reading patches and {@linkplain #readPrettyJson(InputStream, String, boolean, boolean) prettying Json data}.
	 */
	public static final Gson GSON = Patches.patchGson(BASE_CONTEXT)
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
				PatchedInternal.LOGGER.warn("Failed to parse {} as json:", location, e);

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
				PatchedInternal.LOGGER.warn("Failed to parse {} as json:", location, e);
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

	// ================================================================================================

	private static boolean fileResourcesHookWorks = true;

	/**
	 * Returns a list of all resources in the provided pack under the given namespace that match the specified filter.
	 * @param pack The pack to look for resources in.
	 * @param type The type of resources to look for. Most important for mods, which may have both kinds.
	 * @param namespace The namespace to look under.
	 * @param filter A filter for filtering out undesired results.
	 * @return The list of resources.
	 */
	public static List<PatchedResourceLocation> getResources(PatchedPackResources pack, PatchedPackType type, String namespace, Predicate<PatchedResourceLocation> filter) {
		if (pack.patched$isFilePack())
			return fileResourcesHookWorks ? getFileResources(pack, type, namespace, filter) : List.of();

		final List<PatchedResourceLocation> ret = new ArrayList<>();

		// This one's gonna require some explaining:
		// Basically, we want to look at all resources in the pack.
		// The problem is that Minecraft prevents this by bailing for paths "", ".", etc.
		// However, it doesn't check *namespaces*...
		//
		// So what we do here is swap the namespace and path so that it initially
		// resolves the same directory and then resolves the namespace directory.
		// We must use a dot for VanillaPackResources because otherwise LinkFileSystem throws.
		try {
			final Function<PatchedResourceLocation, PatchedResourceLocation> renamer = pack.patched$getRenamer(namespace);
			final String fakeNamespace;
			final String fakePath;

			if (pack.patched$needsSwapNamespaceAndPath()) {
				// The vanilla pack throws on empty paths.
				fakeNamespace = pack.patched$isVanillaPack() ? "." : "";
				fakePath = namespace;
			} else {
				fakeNamespace = namespace;
				fakePath = "";
			}

			pack.patched$listResources(type, fakeNamespace, fakePath, loc -> {
				if (filter.test(loc)) {
					final PatchedResourceLocation renamed = renamer.apply(loc);

					ret.add(renamed);
				}
			});
		} catch (Exception e) {
			PatchedInternal.LOGGER.error("Exception listing resources:", e);
		}

		return ret;
	}

	/**
	 * This method is a better implementation of
	 * {@code FilePackResources#listResources(PackType, String, String, ResourceOutput)}
	 * that actually works for what we need -- getting all resources under a particular namespace.
	 * @param pack The pack in question.
	 * @param type The pack type.
	 * @param namespace The namespace.
	 * @param filter A filter for deciding which resources to include in the returned list.
	 * @return The list of resources under the given namespace.
	 */
	private static List<PatchedResourceLocation> getFileResources(PatchedPackResources pack, PatchedPackType type, String namespace, Predicate<PatchedResourceLocation> filter) {
		final ZipFile zip;
		try {
			zip = pack.patched$getFilePackZipFile();
		} catch (Throwable e) {
			PatchedInternal.LOGGER.error("Accessing FilePackResources ZipFile threw an exception! Listing FilePackResources contents is now disabled. Informational commands for zip packs may not work correctly!", e);
			fileResourcesHookWorks = false;
			return List.of();
		}

		if (zip == null) return List.of();

		final List<PatchedResourceLocation> ret = new ArrayList<>();

		final String root = type.directory + "/" + namespace + "/";

		for (Enumeration<? extends ZipEntry> it = zip.entries(); it.hasMoreElements(); ) {
			final ZipEntry entry = it.nextElement();
			if (entry.isDirectory() || !entry.getName().startsWith(root)) continue;

			final String path = entry.getName().substring(root.length());
			final PatchedResourceLocation loc = PatchedPlatform.get().tryBuild(namespace, path);

			if (loc != null && filter.test(loc))
				ret.add(loc);
		}

		return ret;
	}
}