package net.enderturret.patchedmod.common.internal.test;

import java.io.IOException;
import java.io.InputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patchedmod.common.env.PatchedResourceManager;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.IEnvironment;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * The code behind Patched's test suite, accessible via the {@code /patched debug test} command.
 * @author EnderTurret
 */
public final class PatchedTestHandler {

	private static JsonElement parseJson(PatchedResourceManager resourceManager, String location) throws IOException {
		try (InputStream is = resourceManager.patched$getResource(PatchedPlatform.get().tryParse(location)).get()) {
			return PatchedInternal.readJson(is, location, true);
		}
	}

	private static String parseString(PatchedResourceManager resourceManager, String location) throws IOException {
		try (InputStream is = resourceManager.patched$getResource(PatchedPlatform.get().tryParse(location)).get()) {
			return PatchedInternal.readString(is);
		}
	}

	/**
	 * Runs the test suite in the specified environment.
	 * @param <T> The type representing the command source.
	 * @param env The environment the command is running in.
	 * @param src The command source.
	 */
	public static <T> void runTest(IEnvironment<T> env, T src) {
		env.submit(src, () -> {
			env.sendSuccess(src, false, env.literalText("\n".repeat(90)));

			final PatchedPackType type = env.client() ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA;
			final PatchedResourceManager resourceManager = env.getResourceManager(src);

			boolean result = false;
			try {
				result = runTest(env, src, resourceManager, type, "test");
				if (type == PatchedPackType.CLIENT_RESOURCES)
					result &= runTest(env, src, resourceManager, type, "modtest");
			} catch (Exception e) {
				PatchedInternal.LOGGER.error("Error running tests:", e);
				result = false;
			}

			if (!result) {
				env.sendFailure(src, env.literalText("There were failing tests. Skipping reload."));
				return;
			}

			PatchedInternal.LOGGER.info("Reloading resources!");
			env.sendSuccess(src, false, env.literalText("\nPerforming resource reload!\n"));
			env.reloadResources(src).thenRun(() -> env.submit(src, () -> {
				final PatchedResourceManager resourceManager2 = env.getResourceManager(src);

				boolean result2;
				try {
					result2 = runTest(env, src, resourceManager2, type, "test");
					if (type == PatchedPackType.CLIENT_RESOURCES)
						result2 &= runTest(env, src, resourceManager2, type, "modtest");
				} catch (Exception e) {
					PatchedInternal.LOGGER.error("Error running tests:", e);
					result2 = false;
				}

				PatchedInternal.LOGGER.info("Overall test result: {}", result2 ? "PASSED" : "FAILED");
				env.sendSuccess(src, false, env.literalText("Overall test result: " + (result2 ? "PASSED" : "FAILED")));
			}));
		});
	}

	private static <T> boolean runTest(IEnvironment<T> env, T src, PatchedResourceManager resourceManager, PatchedPackType type, String namespace) throws IOException {
		env.sendSuccess(src, false, env.literalText("Running test suite for " + type + " " + namespace + "!"));

		final JsonArray tests = parseJson(resourceManager, namespace + ":patched/tests.json").getAsJsonArray();
		boolean overall = true;

		for (JsonElement testElem : tests) {
			final JsonObject test = testElem.getAsJsonObject();
			final String name = test.get("name").getAsString();
			final String resultFile = test.get("result").getAsString();
			final boolean result;
			String comparison = "";

			if (test.has("command")) {
				final String command = test.get("command").getAsString();
				final String expected = parseString(resourceManager, resultFile) + "\n";
				final String actual = env.executeCommand(src, command);
				result = expected.equals(actual);
				if (!result) comparison = "Expected <<<" + expected + ">>>, got <<<" + actual + ">>>";
			} else {
				final String inputFile = test.get("input").getAsString();
				final JsonElement expected = parseJson(resourceManager, resultFile);
				final JsonElement actual = parseJson(resourceManager, inputFile);
				result = expected.equals(actual);
				if (!result) comparison = "Expected <<<" + PatchedInternal.GSON.toJson(expected) + ">>>, got <<<" + PatchedInternal.GSON.toJson(actual) + ">>>";
			}

			overall &= result;
			if (result)
				env.sendSuccess(src, false, env.literalText("Test " + name + " passed"));
			else
				env.sendFailure(src, env.literalText("Test " + name + " ").appendWithHover("failed", comparison));
		}

		return overall;
	}
}