package net.enderturret.patchedmod.common.internal.env;

import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.PatchedTestEvaluator;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

/**
 * An abstraction over the different loaders Patched supports.
 * @author EnderTurret
 */
@Internal
public interface PatchedPlatform {

	/**
	 * Patched's mod ID.
	 */
	public static final String MOD_ID = "patched";

	/**
	 * Returns Patched's platform instance.
	 * @return The platform.
	 */
	public static PatchedPlatform get() {
		if (PatchedInternal.platform == null) {
			PatchedInternal.platform = ServiceLoader.load(PatchedPlatform.class)
					.iterator().next();
			PatchedTestEvaluator.registerDefaults();
		}

		return PatchedInternal.platform;
	}

	/**
	 * <p>
	 * Returns whether or not the platform contains {@linkplain PatchedPackResources#patched$isGroupPack() group packs}.
	 * </p>
	 * <p>
	 * Some of the Patched machinery uses this method to determine whether to enable more expensive code paths that aren't necessary on newer versions.
	 * </p>
	 * <p>
	 * Platforms where this is {@code true}:
	 * <table border="1">
	 * <tr><th>Platform</th><th>Group pack name</th></tr>
	 * <tr><td>Fabric 1.18.2</td><td>{@code GroupResourcePack}</td></tr>
	 * <tr><td>Fabric 1.19.2</td><td>{@code GroupResourcePack}</td></tr>
	 * <tr><td>Fabric 1.20.1</td><td>{@code GroupResourcePack}</td></tr>
	 * <tr><td>Forge 1.18.2</td><td>{@code DelegatingResourcePack}</td></tr>
	 * <tr><td>Forge 1.19.2</td><td>{@code DelegatingPackResources}</td></tr>
	 * <tr><td>Forge 1.20.1</td><td>{@code DelegatingPackResources}</td></tr>
	 * </table>
	 * </p>
	 * @return {@code true} if so.
	 */
	public default boolean hasGroupPacks() { return false; }

	/**
	 * <p>
	 * Returns whether or not the platform contains the deprecated (and soon-to-be-withdrawn) legacy {@code "patched:has_patches"} format.
	 * </p>
	 * <p>
	 * This is necessary because while a new syntax was introduced in 1.20.4, the old syntax was historically used throughout 1.20.1 and below.
	 * We issue warnings about the deprecated status of this syntax in 1.21.1 and up,
	 * but we wouldn't want to clutter the logs with complaints on versions that predate the newer format.
	 * </p>
	 * @return {@code true} if so.
	 */
	public default boolean hasLegacyPatchedMetadata() { return false; }

	/**
	 * <p>
	 * Returns whether or not the platform contains unprefixed pack IDs.
	 * A prefixed pack ID takes the form "file/<pack name>"; unprefixed ones are simply "<pack name>".
	 * </p>
	 * <p>
	 * This is necessary because while on these versions we do backport the prefixed names,
	 * we also need to know whether to unprefix pack names passed to the {@code patched:pack_enabled} test condition.
	 * </p>
	 * @return {@code true} if so.
	 */
	public default boolean hasUnprefixedPackIds() { return false; }

	/**
	 * Returns whether or not Patched is running on the (physical) client.
	 * @return {@code true} if Patched is running on the client.
	 */
	public boolean isPhysicalClient();

	/**
	 * Returns whether or not Patched is running in a production environment.
	 * @return {@code true} if Patched is running in production.
	 */
	public boolean isProduction();

	/**
	 * Returns whether or not a mod with the specified mod ID is loaded.
	 * @param modId The mod ID to check.
	 * @return {@code true} if the mod is loaded.
	 */
	public boolean isModLoaded(String modId);

	/**
	 * Returns whether or not a mod with the specified mod ID is loaded <i>and</i> is at least the specified version.
	 * @param modId The mod ID to check.
	 * @param version The minimum version of the mod to require.
	 * @return {@code true} if the mod is loaded and is <i>at least</i> the specified version.
	 */
	public boolean isModLoaded(String modId, String version);

	/**
	 * If the specified pack belongs to a mod, and no {@code pack.mcmeta} exists for it,
	 * this method tries to derive a {@link PatchedMetadata} from the mod's {@code mods.toml} / {@code neoforge.mods.toml} / {@code fabric.mods.json} / {@code quilt.mods.json}.
	 * @param pack The pack in question.
	 * @return A derived {@link PatchedMetadata}, or {@code null} if one could not be derived.
	 */
	public default @Nullable PatchedMetadata deriveMetadataFromMod(PatchedPackResources pack) { return null; }

	/**
	 * A binding to {@link Codec#decode(DynamicOps, Object)}, since in newer DFU versions {@code DataResult} became an interface.
	 * @param <T> The output value type.
	 * @param <I> The input value type.
	 * @param codec The codec for the desired value.
	 * @param ops The {@code DynamicOps} to interpret the input.
	 * @param input The input value.
	 * @return A {@link JankyDataResult} representing the outcome of the operation.
	 */
	public <T, I> JankyDataResult<T> decode(Codec<T> codec, DynamicOps<I> ops, I input);

	/**
	 * A binding to {@link DataResult#success(Object)}, since in newer DFU versions {@code DataResult} became an interface.
	 * @param <T> The value type.
	 * @param value The value.
	 * @return The {@code DataResult}.
	 */
	public <T> DataResult<T> success(T value);

	/**
	 * A binding to {@link DataResult#error(String)}, since in newer DFU versions {@code DataResult} became an interface.
	 * @param <T> The value type.
	 * @param message The error message.
	 * @return The {@code DataResult}.
	 */
	public <T> DataResult<T> error(Supplier<String> message);

	/**
	 * A binding to {@code ResourceLocation.tryParse()}.
	 * @param input The input string.
	 * @return A new {@code ResourceLocation}, or {@code null} if it could not be parsed.
	 */
	public @Nullable PatchedResourceLocation tryParse(String input);

	/**
	 * A binding to {@code ResourceLocation.tryBuild()}.
	 * @param namespace The namespace.
	 * @param path The path.
	 * @return A new {@code ResourceLocation}, or {@code null} if the namespace or path are invalid.
	 */
	public @Nullable PatchedResourceLocation tryBuild(String namespace, String path);

	/**
	 * Returns whether or not the specified ID corresponds to a registry entry in the specified registry.
	 * @param registry The ID of the registry to check.
	 * @param id The ID of the desired entry.
	 * @return {@code true} if {@code id} corresponds to something in {@code registry}.
	 */
	public boolean isThingRegistered(PatchedResourceLocation registry, PatchedResourceLocation id);

	/**
	 * {@code Item} version of {@link #isThingRegistered(PatchedResourceLocation, PatchedResourceLocation)}.
	 * @param id The ID of the desired item.
	 * @return {@code true} if {@code id} corresponds to an item.
	 */
	public boolean isItemRegistered(PatchedResourceLocation id);

	/**
	 * Returned by the output of {@link PatchedPlatform#decode(Codec, DynamicOps, Object)}, since {@code DataResult} has source breaking changes between versions.
	 *
	 * @author EnderTurret
	 *
	 * @param <T> The value type.
	 * @param value The value, if present.
	 * @param error The error message, if present.
	 */
	public static record JankyDataResult<T>(@Nullable T value, @Nullable String error) {}
}