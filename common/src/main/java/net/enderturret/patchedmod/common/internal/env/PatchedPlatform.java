package net.enderturret.patchedmod.common.internal.env;

import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.PatchedTestEvaluator;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

/**
 * An abstraction over the different loaders Patched supports.
 * @author EnderTurret
 */
@Internal
public interface PatchedPlatform {

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
	 * Returns whether or not Patched is running on the (physical) client.
	 * @return {@code true} if Patched is running on the client.
	 */
	public boolean isPhysicalClient();

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
	@Nullable
	public default PatchedMetadata deriveMetadataFromMod(PatchedPackResources pack) {
		return null;
	}

	public <T, I> JankyDataResult<T> decode(Codec<T> codec, DynamicOps<I> ops, I input);
	public <T> DataResult<T> success(T value);
	public <T> DataResult<T> error(Supplier<String> message);

	public PatchedResourceLocation tryParse(String input);
	public PatchedResourceLocation tryBuild(String namespace, String path);
	public boolean isThingRegistered(PatchedResourceLocation registry, PatchedResourceLocation id);
	public boolean isItemRegistered(PatchedResourceLocation id);

	public static record JankyDataResult<T>(@Nullable T value, @Nullable String error) {}
}