package net.enderturret.patchedmod.util.env;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.common.env.IPatchingPackResources;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;

/**
 * An abstraction over the different loaders Patched supports.
 * @author EnderTurret
 */
@Internal
public interface IPlatform {

	/**
	 * Returns Patched's {@code Logger} instance.
	 * @return Patched's {@code Logger} instance.
	 */
	public default Logger logger() { return PatchedInternal.LOGGER; }

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
	 * Returns the {@link PackOutput} of the given {@link DataGenerator}.
	 * @param generator The {@code DataGenerator} to fetch the {@code PackOutput} from.
	 * @return The {@code PackOutput}.
	 */
	public PackOutput getPackOutput(DataGenerator generator);

	/**
	 * <p>
	 * Minecraft generally gives each pack a "name" or "id", like {@code "file/[file name]"} for resource/data packs.
	 * Mod loaders on the other hand give mod resource/data packs... less consistent ones.
	 * </p>
	 *
	 * <p>
	 * In particular (where "mymod" is a mod id):
	 * <table border="1">
	 * <tr><th>Mod loader</th><th>Pack id</th></tr>
	 * <tr><td>Forge</td><td>"My Mod.jar"<sup> [before 20.6]</sup> "mod:mymod"<sup> [since 20.6]</sup></td></tr>
	 * <tr><td>NeoForge</td><td>"mod:mymod"<sup> [before 20.6]</sup> "mod/mymod"<sup> [since 20.6]</sup></td></tr>
	 * <tr><td>Fabric</td><td>"mymod"</td></tr>
	 * <tr><td>Quilt</td><td>"mymod"</td></tr>
	 * </table>
	 * </p>
	 *
	 * <p>
	 * This method allows for changing this to be more consistent across loaders.
	 * The proposed new format is this: {@code "mod/[mod name]"}.
	 * </p>
	 * <p>
	 * The reason we use the mod name instead of the id is because it fits in better with the other pack types' ids (which are named after the files themselves).
	 * Additionally, mod ids are technical identifiers, and so one occasionally ends up with ids like {@code "mcwtrpdoors"} or {@code "shwfox"}.
	 * Mod ids are frequently abbreviated, clipped, or otherwise shortened, such as with {@code "waila"} (acronym) or {@code "everycomp"} (clipped), or either of the aforementioned (shortened).
	 * (Presumably to ease typing out item or block ids.)
	 * It may not be immediately obvious what mods these ids refer to, and in extreme cases the mod could be renamed but retain its old id!
	 * </p>
	 *
	 * <p>
	 * More recently, it has come to my attention that Fabric <i>reuses</i> its resource pack type for mods' builtin ones, too.
	 * For example, {@code "mymod:someoptionalpack"}. The corresponding format we use for these is {@code "mod/[mod name]/[stripped pack id]"}.
	 * </p>
	 *
	 * @param pack The pack in question.
	 * @return The "name" of the pack.
	 */
	public String getName(PackResources pack);

	/**
	 * If the specified pack belongs to a mod, and no {@code pack.mcmeta} exists for it,
	 * this method tries to derive a {@link PatchedMetadata} from the mod's {@code mods.toml} / {@code neoforge.mods.toml} / {@code fabric.mods.json} / {@code quilt.mods.json}.
	 * @param pack The pack in question.
	 * @return A derived {@link PatchedMetadata}, or {@code null} if one could not be derived.
	 */
	@Nullable
	public default PatchedMetadata deriveMetadataFromMod(PackResources pack) {
		return null;
	}

	/**
	 * <p>
	 * Returns whether or not the specified pack is actually a group of packs.
	 * </p>
	 * <p>
	 * In the 1.21 age, no loader has 'group' packs anymore, but in 1.20.1 the were quite prevalent.
	 * This method is used to ensure they get unpacked correctly.
	 * </p>
	 * @param pack The pack to check.
	 * @return {@code true} if the pack is a group pack.
	 */
	@Deprecated(since = "7.4.0+1.21.1", forRemoval = true)
	public default boolean isGroup(PackResources pack) { return false; }

	/**
	 * If the pack is a group, returns the children of the pack.
	 * @param pack The pack to unpack.
	 * @return The pack's children.
	 */
	@Deprecated(since = "7.4.0+1.21.1", forRemoval = true)
	public default Collection<PackResources> getChildren(PackResources pack) { return List.of(); }

	/**
	 * <p>
	 * If the pack is a group, returns the children of the pack that contain the namespace of the specified file.
	 * </p>
	 * <p>
	 * <b>Note</b>: this method doesn't check to see if any of the returned packs <i>actually</i> contain the given file.
	 * It only makes sure the returned packs contain the <i>namespace</i> of the given file.
	 * </p>
	 * @param pack The pack in question.
	 * @param type The pack type.
	 * @param file The file.
	 * @return The list of {@link PackResources} that contain the namespace of the given file.
	 */
	@Deprecated(since = "7.4.0+1.21.1", forRemoval = true)
	public default Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, Identifier file) { return List.of(); }

	/**
	 * Determines whether the specified pack needs the namespace and path flipped in order to discover all files in a given namespace.
	 * In short, this determines whether to ask for ":minecraft" (flipped) or "minecraft:" (normal).
	 * This is necessary because {@link PathPackResources} throws for empty/dotted paths (so we trick it by putting the namespace in the path).
	 * @param pack The pack in question.
	 * @return {@code true} if the namespace and path must be swapped.
	 */
	public boolean needsSwapNamespaceAndPath(PackResources pack);

	/**
	 * As a consequence of {@link #needsSwapNamespaceAndPath(PackResources)}, the returned {@linkplain Identifier identifiers} may need to be renamed.
	 * This method returns the renamer function for a given pack.
	 * @param pack The pack in question.
	 * @param namespace The namespace being searched.
	 * @return The renamer function.
	 */
	public Function<Identifier, Identifier> getRenamer(PackResources pack, String namespace);

	/**
	 * Returns a {@code Stream} over all packs in the given resource manager, expanding {@linkplain #getChildren(PackResources) group packs} as necessary.
	 * @param manager The resource manager to query the packs of.
	 * @return The stream.
	 */
	@Deprecated(since = "7.4.0+1.21.1", forRemoval = true)
	public default Stream<PackResources> getExpandedPacks(ResourceManager manager) {
		return manager.listPacks()
				.flatMap(p -> isGroup(p) ? getChildren(p).stream() : Stream.of(p));
	}

	/**
	 * Returns a {@code Stream} over all patching-enabled packs in the given resource manager, expanding {@linkplain #getChildren(PackResources) group packs} as necessary.
	 * This functions like {@link #getExpandedPacks(ResourceManager)}, but additionally filtering out non-patching packs.
	 * @param manager The resource manager to query the packs of.
	 * @return The stream.
	 */
	public default Stream<PackResources> getPatchingPacks(ResourceManager manager) {
		return manager.listPacks().filter(this::hasPatches);
	}

	/**
	 * A convenience method to check whether or not the specified pack has patching enabled.
	 * @param pack The pack in question.
	 * @return {@code true} if the pack has patching enabled.
	 */
	public default boolean hasPatches(PackResources pack) {
		return pack instanceof IPatchingPackResources ppp && ppp.patchedMetadata().patchingEnabled();
	}
}