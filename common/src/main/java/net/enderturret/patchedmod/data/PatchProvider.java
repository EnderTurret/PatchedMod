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
import com.google.gson.JsonNull;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.CompoundPatch;
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
	 * <p>
	 * Serializes the specified {@code Object}.
	 * </p>
	 * <p>
	 * This method is used by the {@link OperationBuilder} methods, and allows configuring how objects are serialized to json.
	 * For example, one could set up a custom {@link Gson} instance with serializers registered for custom objects and use that here,
	 * which would remove the need to turn them into {@code JsonElement}s before calling these methods.
	 * </p>
	 * @implNote By default this uses a built-in internal {@code Gson} instance.
	 * This {@code Gson} is <i>not</i> configured for Minecraft's many {@code Object}s, so changing it out for a different one is not a bad idea.
	 * @param obj The object to serialize.
	 * @return The serialized representation of the object.
	 */
	protected JsonElement serialize(@Nullable Object obj) {
		return GSON.toJsonTree(obj);
	}

	/**
	 * Conveniently constructs a {@link ResourceLocation} using the given arguments.
	 * @param modId The mod id or domain of the {@link ResourceLocation}.
	 * @param path The path of the {@link ResourceLocation}.
	 * @return The new {@link ResourceLocation}.
	 */
	public ResourceLocation id(String modId, String path) {
		return ResourceLocation.fromNamespaceAndPath(modId, path);
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

	/**
	 * Represents a builder for a {@link CompoundPatch}.
	 * The patch in question may be either the root {@code CompoundPatch} of a json patch file,
	 * or a child of another {@code CompoundPatch}.
	 * @author EnderTurret
	 */
	public sealed abstract class OperationBuilder permits RootOperationBuilder, ChildOperationBuilder {

		OperationBuilder() {}

		/**
		 * Adds the specified patch to the {@link CompoundPatch}'s children.
		 * @param patch The patch to add.
		 * @return {@code this}.
		 */
		protected abstract OperationBuilder save(JsonPatch patch);

		/**
		 * Indicates the end of this patch.
		 * This may be used to terminate {@linkplain #compound() compound patches}.
		 * Calling this on the root builder does nothing.
		 * @return The parent builder.
		 */
		public abstract OperationBuilder end();

		// Simple path/value patches

		private static <T> JsonElement serializeUnchecked(@Nullable T value, Codec<T> codec, @Nullable HolderLookup.Provider provider) {
			if (value == null) return JsonNull.INSTANCE;

			DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
			if (provider != null)
				ops = provider.createSerializationContext(JsonOps.INSTANCE);

			final DataResult<JsonElement> result = codec.encodeStart(ops, value);

			return result.getOrThrow(PatchingException::new);
		}

		/**
		 * Creates and adds a new {@code add} patch.
		 * @param path The location the element will be placed.
		 * @param value The element that will be added. It must be either a {@link JsonElement} of some kind, or an {@code Object} that the {@code PatchProvider} can serialize.
		 * @return {@code this}.
		 */
		public OperationBuilder add(String path, Object value) {
			return save(PatchUtil.add(path, PatchProvider.this.serialize(value)));
		}

		/**
		 * Creates and adds a new {@code add} patch.
		 * @param path The location the element will be placed.
		 * @param value The element that will be added.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @return {@code this}.
		 * @deprecated Use {@link #add(String, Object, Codec, net.minecraft.core.HolderLookup.Provider)} instead.
		 */
		@Deprecated(forRemoval = true, since = "7.1.1+1.21.1")
		public <T> OperationBuilder add(String path, T value, Codec<T> valueCodec) {
			return add(path, value, valueCodec, null);
		}

		/**
		 * Creates and adds a new {@code add} patch.
		 * @param path The location the element will be placed.
		 * @param value The element that will be added.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @param provider Required to create a {@link RegistryOps}. Otherwise, passing in {@code null} will just use a regular {@link JsonOps}.
		 * @return {@code this}.
		 */
		public <T> OperationBuilder add(String path, T value, Codec<T> valueCodec, @Nullable HolderLookup.Provider provider) {
			return save(PatchUtil.add(path, serializeUnchecked(value, valueCodec, provider)));
		}

		/**
		 * Creates and adds a new {@code replace} patch.
		 * @param path The path to the element to replace.
		 * @param value The value to replace the element with. It must be either a {@link JsonElement} of some kind, or an {@code Object} that the {@code PatchProvider} can serialize.
		 * @return {@code this}.
		 */
		public OperationBuilder replace(String path, Object value) {
			return save(PatchUtil.replace(path, PatchProvider.this.serialize(value)));
		}

		/**
		 * Creates and adds a new {@code replace} patch.
		 * @param path The path to the element to replace.
		 * @param value The value to replace the element with.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @return {@code this}.
		 * @deprecated Use {@link #replace(String, Object, Codec, net.minecraft.core.HolderLookup.Provider)} instead.
		 */
		@Deprecated(forRemoval = true, since = "7.1.1+1.21.1")
		public <T> OperationBuilder replace(String path, T value, Codec<T> valueCodec) {
			return replace(path, value, valueCodec, null);
		}

		/**
		 * Creates and adds a new {@code replace} patch.
		 * @param path The path to the element to replace.
		 * @param value The value to replace the element with.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @param provider Required to create a {@link RegistryOps}. Otherwise, passing in {@code null} will just use a regular {@link JsonOps}.
		 * @return {@code this}.
		 */
		public <T> OperationBuilder replace(String path, T value, Codec<T> valueCodec, @Nullable HolderLookup.Provider provider) {
			return save(PatchUtil.replace(path, serializeUnchecked(value, valueCodec, provider)));
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
		 * @param value The test element. May be {@code null}. If non-{@code null}, it must be either a {@link JsonElement} of some kind, or an {@code Object} that the {@code PatchProvider} can serialize.
		 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
		 * @return {@code this}.
		 */
		public OperationBuilder test(String type, @Nullable String path, @Nullable Object value, boolean inverse) {
			return save(PatchUtil.test(type, path, PatchProvider.this.serialize(value), inverse));
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param type A custom type for {@link ITestEvaluator}.
		 * @param value The test element. It must be either a {@link JsonElement} of some kind, or an {@code Object} that the {@code PatchProvider} can serialize.
		 * @return {@code this}.
		 */
		public OperationBuilder test(String type, Object value) {
			return test(type, null, value, false);
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param path The path to the element to test.
		 * @param value The test element. May be {@code null}. If non-{@code null}, it must be either a {@link JsonElement} of some kind, or an {@code Object} that the {@code PatchProvider} can serialize.
		 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
		 * @return {@code this}.
		 */
		public OperationBuilder test(String path, @Nullable Object value, boolean inverse) {
			return save(PatchUtil.test(path, PatchProvider.this.serialize(value), inverse));
		}

		// Test (Codecs)

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param type A custom type for {@link ITestEvaluator}.
		 * @param path The path to the element to test. May be {@code null}.
		 * @param value The test element. May be {@code null}.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
		 * @return {@code this}.
		 * @deprecated Use {@link #test(String, String, Object, Codec, net.minecraft.core.HolderLookup.Provider, boolean)} instead.
		 */
		@Deprecated(forRemoval = true, since = "7.1.1+1.21.1")
		public <T> OperationBuilder test(String type, @Nullable String path, @Nullable T value, Codec<T> valueCodec, boolean inverse) {
			return test(type, path, value, valueCodec, null, inverse);
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param type A custom type for {@link ITestEvaluator}.
		 * @param value The test element.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @return {@code this}.
		 * @deprecated Use {@link #test(String, Object, Codec, net.minecraft.core.HolderLookup.Provider)} instead.
		 */
		@Deprecated(forRemoval = true, since = "7.1.1+1.21.1")
		public <T> OperationBuilder test(String type, T value, Codec<T> valueCodec) {
			return test(type, value, valueCodec, null);
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param path The path to the element to test.
		 * @param value The test element. May be {@code null}.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
		 * @return {@code this}.
		 * @deprecated Use {@link #test(String, Object, Codec, net.minecraft.core.HolderLookup.Provider, boolean)} instead.
		 */
		@Deprecated(forRemoval = true, since = "7.1.1+1.21.1")
		public <T> OperationBuilder test(String path, @Nullable T value, Codec<T> valueCodec, boolean inverse) {
			return test(path, value, valueCodec, null, inverse);
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param type A custom type for {@link ITestEvaluator}.
		 * @param path The path to the element to test. May be {@code null}.
		 * @param value The test element. May be {@code null}.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @param provider Required to create a {@link RegistryOps}. Otherwise, passing in {@code null} will just use a regular {@link JsonOps}.
		 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
		 * @return {@code this}.
		 */
		public <T> OperationBuilder test(String type, @Nullable String path, @Nullable T value, Codec<T> valueCodec, @Nullable HolderLookup.Provider provider, boolean inverse) {
			return save(PatchUtil.test(type, path, serializeUnchecked(value, valueCodec, provider), inverse));
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param type A custom type for {@link ITestEvaluator}.
		 * @param value The test element.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @param provider Required to create a {@link RegistryOps}. Otherwise, passing in {@code null} will just use a regular {@link JsonOps}.
		 * @return {@code this}.
		 */
		public <T> OperationBuilder test(String type, T value, Codec<T> valueCodec, @Nullable HolderLookup.Provider provider) {
			return test(type, null, value, valueCodec, provider, false);
		}

		/**
		 * Creates and adds a new {@code test} patch.
		 * @param path The path to the element to test.
		 * @param value The test element. May be {@code null}.
		 * @param valueCodec A {@code Codec} for turning {@code value} into json.
		 * @param provider Required to create a {@link RegistryOps}. Otherwise, passing in {@code null} will just use a regular {@link JsonOps}.
		 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
		 * @return {@code this}.
		 */
		public <T> OperationBuilder test(String path, @Nullable T value, Codec<T> valueCodec, @Nullable HolderLookup.Provider provider, boolean inverse) {
			return save(PatchUtil.test(path, serializeUnchecked(value, valueCodec, provider), inverse));
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
		 * @param tests A list of tests that an element must pass to have {@code then} applied to it. A {@code TestPatch} instance can be obtained using {@link PatchUtil#test(String, JsonElement, boolean)} or {@link PatchUtil#test(String, String, JsonElement, boolean)}.
		 * @param then A patch to apply to elements passing the tests. See {@link PatchUtil} for methods to obtain a {@code JsonPatch} instance.
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