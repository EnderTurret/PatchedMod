package net.enderturret.patchedmod.util.meta;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;

import net.enderturret.patched.exception.PatchingException;

/**
 * <p>
 * {@code PatchedMetadata} is the decoded form of the {@code patched} block of metadata in {@code pack.mcmeta} files.
 * </p>
 * <p>
 * For example, this contains a {@code PatchedMetadata} section:
 * <pre><code> {
 *   "pack": {
 *     "description": "This is a data or resource pack that uses Patched!",
 *     "pack_format": 15
 *   },
 *   "patched": {
 *     "format_version": 1
 *   }
 * }</code></pre>
 * </p>
 * @param formatVersion Specifies the Patched metadata schema version in use. {@code -1} means the pack doesn't use Patched, and {@code 0} is the legacy {@code "patched:has_patches"} format.
 * @param patchTargets A list of patch "targets" which allow applying the same patch to multiple files.
 * @author EnderTurret
 */
public record PatchedMetadata(byte formatVersion, List<PatchTarget> patchTargets) {

	public PatchedMetadata {
		if (formatVersion < -1) throw new IllegalArgumentException("Format version must be in range [-1, 147], was: " + formatVersion);
	}

	public PatchedMetadata(byte formatVersion) {
		this(formatVersion, List.of());
	}

	public static final Codec<PatchedMetadata> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			// It isn't great, but it works.
			Codec.intRange(1, 147).xmap(Integer::byteValue, Byte::intValue).fieldOf("format_version").forGetter(PatchedMetadata::formatVersion),

			PatchTarget.CODEC.listOf().optionalFieldOf("patch_targets", List.of()).forGetter(PatchedMetadata::patchTargets)
			).apply(builder, PatchedMetadata::new));

	/**
	 * Returns whether or not Patched should look for patches in a pack with this {@code PatchedMetadata}.
	 * @return {@code true} if Patched should look for patches.
	 */
	public boolean patchingEnabled() {
		return formatVersion >= 0;
	}

	/**
	 * The cached metadata for packs that don't use Patched in any way.
	 * These have a format version of negative one (-1).
	 */
	public static final PatchedMetadata DISABLED_METADATA = new PatchedMetadata((byte) -1);

	/**
	 * The cached metadata for packs that use the older hacked-into-the-pack-metadata-section format, like so:
	 * <pre><code> {
	 *   "pack": {
	 *     "description": "This is a pack made for an older version of Patched!",
	 *     "pack_format": 12,
	 *     "patched:has_patches": true
	 *   }
	 * }<code></pre>
	 */
	public static final PatchedMetadata LEGACY_METADATA = new PatchedMetadata((byte) 0);

	/**
	 * The cached metadata for state-of-the-art Patched packs that don't make use of Patched metadata beyond the format version.
	 */
	public static final PatchedMetadata CURRENT_VERSION = new PatchedMetadata((byte) 1);

	/**
	 * Attempts to parse a {@code PatchedMetadata} section in the specified {@code pack.mcmeta} contents.
	 * @param elem The contents of a {@code pack.mcmeta} file.
	 * @param source Something that identifies the source of the file, such as the name of the pack containing it. Used for informative error messages.
	 * @return The parsed {@code PatchedMetadata}.
	 * @throws PatchingException If the metadata could not be parsed.
	 */
	public static PatchedMetadata of(@Nullable JsonElement elem, String source) {
		if (elem instanceof JsonObject root) {
			if (root.get("patched") instanceof JsonObject patched) {
				if (patched.has("format_version") && patched.get("format_version") instanceof JsonPrimitive prim
						&& prim.isNumber() && prim.getAsInt() > CURRENT_VERSION.formatVersion)
					throw new PatchingException("Format version " + prim.getAsInt() + " too new! This version of Patched can only load up to version " + CURRENT_VERSION.formatVersion + ".");

				final DataResult<Pair<PatchedMetadata, JsonElement>> pair = CODEC.decode(JsonOps.INSTANCE, patched);

				if (pair.result().isPresent()) {
					final PatchedMetadata ret = pair.result().get().getFirst();
					// Use the cached version when possible; I don't necessarily trust the JVM to do that record optimization (too many people abuse them -- me included).
					return CURRENT_VERSION.equals(ret) ? CURRENT_VERSION : ret;
				}

				throw new PatchingException("Could not parse Patched metadata for " + source + ": " + pair.error().get().message());
			}
			else if (root.get("pack") instanceof JsonObject pack
					&& pack.get("patched:has_patches") instanceof JsonPrimitive hasPatches
					&& hasPatches.isBoolean() && hasPatches.getAsBoolean())
				return LEGACY_METADATA;
		}

		return DISABLED_METADATA;
	}
}