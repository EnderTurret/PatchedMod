package net.enderturret.patchedmod.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchUtil;
import net.enderturret.patched.patch.TestPatch;

/**
 * A data provider for patches.
 * You can use this to datagen Json patches.
 * @author EnderTurret
 */
public abstract class PatchProvider implements DataProvider {

	private static final Gson GSON = net.enderturret.patchedmod.util.PatchUtil.GSON;

	private final DataGenerator generator;

	private final String modId;

	private final Map<ResourceLocation, JsonPatch> patches = new HashMap<>();

	protected PatchProvider(DataGenerator generator, @Nullable String modId) {
		this.generator = generator;
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
		return "Json Patches: " + modId;
	}

	@Override
	public void run(HashCache cache) throws IOException {
		patches.clear();
		registerPatches();

		if (!patches.isEmpty()) {
			final Path root = generator.getOutputFolder();
			for (Map.Entry<ResourceLocation, JsonPatch> entry : patches.entrySet())
				writePatch(cache, root, entry.getKey(), entry.getValue());
		}
	}

	private void writePatch(HashCache cache, Path root, ResourceLocation path, JsonPatch patch) throws IOException {
		final Path to = root.resolve(path.getNamespace()).resolve(path.getPath() + ".json.patch");
		// The ordering is guaranteed to be stable, as patches are serialized manually.
		// Using this method prevents the "type" field of test patches from jumping to the top of the json object.
		write(cache, GSON.toJsonTree(patch), to);
	}

	@SuppressWarnings("deprecation")
	private static void write(HashCache cache, JsonElement elem, Path to) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				HashingOutputStream hos = new HashingOutputStream(Hashing.sha1(), baos);
				OutputStreamWriter osw = new OutputStreamWriter(hos, StandardCharsets.UTF_8);
				JsonWriter jw = new JsonWriter(osw)) {
			jw.setSerializeNulls(false);
			jw.setIndent("  ");
			writeValue(jw, elem);
			jw.close();
			cache.putNew(to, hos.hash().toString());
		}
	}

	// This is copied from newer versions of GsonHelper.
	private static void writeValue(JsonWriter writer, @Nullable JsonElement element) throws IOException {
		if (element != null && !element.isJsonNull()) {
			if (element.isJsonPrimitive()) {
				JsonPrimitive prim = element.getAsJsonPrimitive();
				if (prim.isNumber())
					writer.value(prim.getAsNumber());
				else if (prim.isBoolean())
					writer.value(prim.getAsBoolean());
				else
					writer.value(prim.getAsString());
			} else if (element.isJsonArray()) {
				writer.beginArray();

				for (JsonElement e : element.getAsJsonArray())
					writeValue(writer, e);

				writer.endArray();
			} else {
				if (!element.isJsonObject())
					throw new IllegalArgumentException("Couldn't write " + element.getClass());

				writer.beginObject();

				for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
					writer.name(entry.getKey());
					writeValue(writer, entry.getValue());
				}

				writer.endObject();
			}
		} else
			writer.nullValue();
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