package net.enderturret.patchedmod.util.env;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.util.IPatchingPackResources;

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
	public Logger logger();

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
	 * <tr><td>Forge</td><td>"mod:mymod"</td></tr>
	 * <tr><td>NeoForge</td><td>"mod:mymod"<sup> [previously]</sup> "mod/mymod"<sup> [since 20.6]</sup></td></tr>
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
	 * <p>
	 * Returns whether or not the specified pack is actually a group of packs.
	 * </p>
	 * <p>
	 * Nowadays, no loader has 'group' packs, but in the past they used to be quite prevalent.
	 * This method is used to ensure they get unpacked correctly.
	 * </p>
	 * @param pack The pack to check.
	 * @return {@code true} if the pack is a group pack.
	 */
	public default boolean isGroup(PackResources pack) { return false; }

	/**
	 * If the pack is a group, returns the children of the pack.
	 * @param pack The pack to unpack.
	 * @return The pack's children.
	 */
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
	public default Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, ResourceLocation file) { return List.of(); }

	public boolean needsSwapNamespaceAndPath(PackResources pack);
	public Function<ResourceLocation, ResourceLocation> getRenamer(PackResources pack, String namespace);

	public default Stream<PackResources> getExpandedPacks(ResourceManager manager) {
		return manager.listPacks()
				.flatMap(p -> isGroup(p) ? getChildren(p).stream() : Stream.of(p));
	}

	public default Stream<PackResources> getPatchingPacks(ResourceManager manager) {
		return getExpandedPacks(manager).filter(this::hasPatches);
	}

	public default boolean hasPatches(PackResources pack) {
		return pack instanceof IPatchingPackResources ppp && ppp.patchedMetadata().patchingEnabled();
	}
}