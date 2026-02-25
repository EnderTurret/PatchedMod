package net.enderturret.patchedmod.common.internal.env.binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;

import net.enderturret.patchedmod.common.env.IPatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Patched's bindings to {@code PackResources}.
 * @author EnderTurret
 */
public interface PatchedPackResources extends IPatchingPackResources {

	// ===== PackResources bindings =====

	public String patched$packId();
	public boolean patched$isVanillaPack();
	public boolean patched$isFilePack();
	public ZipFile patched$getFilePackZipFile();

	public Set<String> patched$getNamespaces(PatchedPackType type);
	public @Nullable InputStream patched$getRootResource(String... path) throws IOException;
	public @Nullable InputStream patched$getResource(PatchedPackType type, PatchedResourceLocation location) throws IOException;
	public void patched$listResources(PatchedPackType type, String namespace, String path, Consumer<PatchedResourceLocation> consumer);

	public boolean patched$hasRootResource(String... path);
	public boolean patched$hasResource(PatchedPackType type, PatchedResourceLocation location);

	// ===== Non-API Patched-specific junk =====

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
	 * @return The "name" of the pack.
	 */
	public String patched$getName();

	/**
	 * <p>
	 * Returns whether or not this pack is actually a group of packs.
	 * </p>
	 * <p>
	 * In the 1.21 age, no loader has 'group' packs anymore, but in 1.20.1 the were quite prevalent.
	 * This method is used to ensure they get unpacked correctly.
	 * </p>
	 * @return {@code true} if the pack is a group pack.
	 */
	public default boolean patched$isGroupPack() { return false; }

	/**
	 * If this pack is {@linkplain #patched$isGroupPack() a group}, returns the children of the pack.
	 * @return The pack's children.
	 */
	public default Collection<PatchedPackResources> patched$getChildren() { return List.of(); }

	/**
	 * <p>
	 * If the pack is a group, returns the children of the pack that contain the specified namespace.
	 * </p>
	 * @param type The pack type.
	 * @param namespace The namespace.
	 * @return The list of {@link IPatchingPackResources} that contain the namespace of the given file.
	 */
	public default Collection<PatchedPackResources> patched$getFilteredChildren(PatchedPackType type, String namespace) { return List.of(); }

	/**
	 * Determines whether this pack needs the namespace and path flipped in order to discover all files in a given namespace.
	 * In short, this determines whether to ask for ":minecraft" (flipped) or "minecraft:" (normal).
	 * This is necessary because {@code PathPackResources} throws for empty/dotted paths (so we trick it by putting the namespace in the path).
	 * @return {@code true} if the namespace and path must be swapped.
	 */
	public boolean patched$needsSwapNamespaceAndPath();

	/**
	 * As a consequence of {@link #patched$needsSwapNamespaceAndPath()}, the returned {@linkplain PatchedResourceLocation locations} may need to be renamed.
	 * This method returns the renamer function for the pack.
	 * @param namespace The namespace being searched.
	 * @return The renamer function.
	 */
	public Function<PatchedResourceLocation, PatchedResourceLocation> patched$getRenamer(String namespace);
}