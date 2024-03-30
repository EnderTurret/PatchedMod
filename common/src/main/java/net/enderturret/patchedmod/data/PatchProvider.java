package net.enderturret.patchedmod.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchUtil;
import net.enderturret.patched.patch.TestPatch;
import net.enderturret.patchedmod.Patched;

/**
 * A data provider for patches.
 * You can use this to datagen Json patches.
 * @author EnderTurret
 */
public abstract class PatchProvider implements DataProvider {

	private static final Gson GSON = net.enderturret.patchedmod.util.PatchUtil.GSON;

	private final DataGenerator generator;

	private final PackOutput.Target target;
	private final String modId;

	private final Map<ResourceLocation, JsonPatch> patches = new HashMap<>();

	protected PatchProvider(DataGenerator generator, PackOutput.Target target, @Nullable String modId) {
		if (target == null || target == PackOutput.Target.REPORTS) throw new IllegalArgumentException("Bad type");
		this.generator = generator;
		this.target = target;
		this.modId = modId;
	}

	/**
	 * Use this method to create and register your patches for data generation.
	 */
	public abstract void registerPatches();

	/**
	 * Conveniently constructs a {@link ResourceLocation} using the given arguments.
	 * @param modId The mod id or domain of the {@link ResourceLocation}.
	 * @param path The path of the {@link ResourceLocation}.
	 * @return The new {@link ResourceLocation}.
	 */
	public ResourceLocation id(String modId, String path) {
		return new ResourceLocation(modId, path);
	}

	/**
	 * Conveniently constructs a {@link ResourceLocation} with the given path under the mod id passed in the constructor.
	 * @param path The path of the {@link ResourceLocation}.
	 * @return The new {@link ResourceLocation}.
	 */
	public ResourceLocation id(String path) {
		return id(modId, path);
	}

	@Override
	public String getName() {
		return target + " Json Patches: " + modId;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		patches.clear();
		registerPatches();

		if (!patches.isEmpty()) {
			final Path root = Patched.platform().getPackOutput(generator).getOutputFolder(target);
			final List<CompletableFuture<?>> futures = new ArrayList<>();

			for (Map.Entry<ResourceLocation, JsonPatch> entry : patches.entrySet())
				futures.add(writePatch(cache, root, entry.getKey(), entry.getValue()));

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		}

		return CompletableFuture.allOf();
	}

	private CompletableFuture<?> writePatch(CachedOutput cache, Path root, ResourceLocation path, JsonPatch patch) {
		final Path to = root.resolve(path.getNamespace()).resolve(path.getPath() + ".json.patch");
		// The ordering is guaranteed to be stable, as patches are serialized manually.
		// Using this method prevents the "type" field of test patches from jumping to the top of the json object.
		return CompletableFuture.runAsync(() -> write(cache, GSON.toJsonTree(patch), to), Util.backgroundExecutor());
	}

	@SuppressWarnings("deprecation")
	private static void write(CachedOutput cache, JsonElement elem, Path to) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				HashingOutputStream hos = new HashingOutputStream(Hashing.sha1(), baos);
				OutputStreamWriter osw = new OutputStreamWriter(hos, StandardCharsets.UTF_8);
				JsonWriter jw = new JsonWriter(osw)) {
			jw.setSerializeNulls(false);
			jw.setIndent("  ");
			GsonHelper.writeValue(jw, elem, null);
			jw.close();
			cache.writeIfNeeded(to, baos.toByteArray(), hos.hash());
		} catch (IOException e) {
			Patched.platform().logger().error("Exception saving file to {}:", to, e);
		}
	}

	// ────────────────────────────────────────────────────────────────────────────────────────────────────────────────

	/**
	 * Begin a new patch definition.
	 * @param location The location of the file this patch is patching. See {@link #id(String, String)} for easy {@link ResourceLocation} construction.
	 * @return The patch builder.
	 */
	public OperationBuilder patch(ResourceLocation location) {
		return new RootOperationBuilder(location);
	}

	public abstract class OperationBuilder {

		OperationBuilder() {}

		protected abstract OperationBuilder save(JsonPatch patch);

		/**
		 * Indicates the end of this patch.
		 * This may be used to terminate {@linkplain #compound() compound patches}.
		 * @return The parent builder.
		 */
		public abstract OperationBuilder end();

		// Simple path/value patches

		/**
		 * Creates and adds a new {@code add} patch.
		 * @param path The location the element will be placed.
		 * @param value The element that will be added.
		 * @return {@code this}.
		 */
		public OperationBuilder add(String path, Object value) {
			return save(PatchUtil.add(path, GSON.toJsonTree(value)));
		}

		/**
		 * Creates and adds a new {@code replace} patch.
		 * @param path The path to the element to replace.
		 * @param value The value to replace the element with.
		 * @return {@code this}.
		 */
		public OperationBuilder replace(String path, Object value) {
			return save(PatchUtil.replace(path, GSON.toJsonTree(value)));
		}

		/**
		 * Creates and adds a new {@code remove} patch.
		 * @param path The path to the element to remove.
		 * @return {@code this}.
		 */
		public OperationBuilder remove(String path) {
			return save(PatchUtil.remove(path));
		}

		/**
		 * Creates and adds a new {@code include} patch.
		 * @param path The path to the patch file to include.
		 * @return {@code this}.
		 */
		public OperationBuilder include(String path) {
			return save(PatchUtil.include(path));
		}

		// Path + from patches

		/**
		 * Creates and adds a new {@code copy} patch.
		 * @param path The location the element will be copied to.
		 * @param from The path to the element to copy.
		 * @return {@code this}.
		 */
		public OperationBuilder copy(String path, String from) {
			return save(PatchUtil.copy(path, from));
		}

		/**
		 * Creates and adds a new {@code move} patch.
		 * @param path The location the element will be moved to.
		 * @param from The path to the element to move.
		 * @return {@code this}.
		 */
		public OperationBuilder move(String path, String from) {
			return save(PatchUtil.move(path, from));
		}

		// Test

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param type A custom type for {@link ITestEvaluator}.
		 * @param path The path to the element to test. May be {@code null}.
		 * @param value The test element. May be {@code null}.
		 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
		 * @return {@code this}.
		 */
		public OperationBuilder test(String type, @Nullable String path, @Nullable Object value, boolean inverse) {
			return save(PatchUtil.test(type, path, GSON.toJsonTree(value), inverse));
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param type A custom type for {@link ITestEvaluator}.
		 * @param value The test element.
		 * @return {@code this}.
		 */
		public OperationBuilder test(String type, Object value) {
			return test(type, null, value, false);
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param path The path to the element to test.
		 * @param value The test element. May be {@code null}.
		 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
		 * @return {@code this}.
		 */
		public OperationBuilder test(String path, @Nullable Object value, boolean inverse) {
			return save(PatchUtil.test(path, GSON.toJsonTree(value), inverse));
		}

		// Snowflakes

		/**
		 * Begins a new compound patch.
		 * @return The compound builder.
		 */
		public OperationBuilder compound() {
			return new ChildOperationBuilder(this);
		}

		/**
		 * Creates and adds a new {@code find} patch.
		 * @param path The path to the element to find things in.
		 * @param tests A list of tests that an element must pass to have {@code then} applied to it.
		 * @param then A patch to apply to elements passing the tests.
		 * @param multi Whether to continue searching for matching elements after the first one is found.
		 * @return {@code this}.
		 */
		public OperationBuilder find(String path, List<TestPatch> tests, JsonPatch then, boolean multi) {
			return save(PatchUtil.find(path, tests, then, multi));
		}
	}

	final class ChildOperationBuilder extends OperationBuilder {

		private final OperationBuilder parent;
		private final List<JsonPatch> patches = new ArrayList<>();

		ChildOperationBuilder(OperationBuilder parent) {
			this.parent = parent;
		}

		@Override
		protected OperationBuilder save(JsonPatch patch) {
			patches.add(patch);
			return this;
		}

		@Override
		public OperationBuilder end() {
			parent.save(PatchUtil.compound(patches.toArray(JsonPatch[]::new)));
			return parent;
		}
	}

	final class RootOperationBuilder extends OperationBuilder {

		private final ResourceLocation location;

		RootOperationBuilder(ResourceLocation location) {
			this.location = location;
		}

		@Override
		protected OperationBuilder save(JsonPatch patch) {
			PatchProvider.this.patches.put(location, patch);
			return this;
		}

		@Override
		public OperationBuilder end() {
			return this;
		}
	}
}