package net.enderturret.patchedmod.util.env;

import java.util.Collection;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.util.IPatchingPackResources;

@Internal
public interface IPlatform {

	public Logger logger();

	public boolean isPhysicalClient();
	public boolean isModLoaded(String modId);

	/**
	 * <p>Minecraft generally gives each pack a "name" or "id", like {@code "file/[file name]"} for resource/data packs.
	 * Mod loaders on the other hand give mod resource/data packs... less consistent results.</p>
	 * <p>In particular:
	 * <table border="1">
	 * <tr><th>Mod loader</th><th>Pack id</th></tr>
	 * <tr><td>Forge</td><td>"My Mod.jar" (mod file name)</td></tr>
	 * <tr><td>Fabric</td><td>"mymod" (mod id)</td></tr>
	 * </table></p>
	 * <p>This method allows for changing this to be more consistent across loaders.
	 * The proposed new format is this: {@code "mod/[mod name]"}</p>
	 * @param pack The pack in question.
	 * @return The "name" of the pack.
	 */
	public String getName(PackResources pack);

	public boolean isGroup(PackResources pack);
	public Collection<PackResources> getChildren(PackResources pack);

	/**
	 * Note: this method doesn't check to see if any of the returned packs <i>actually</i> contain the given file.
	 * It only makes sure the returned packs contain the <i>namespace</i> of the given file.
	 * @param pack The pack in question.
	 * @param type The pack type.
	 * @param file The file.
	 * @return The list of {@link PackResources} that contain the namespace of the given file.
	 */
	public Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, ResourceLocation file);

	public default Stream<PackResources> getExpandedPacks(ResourceManager manager) {
		return manager.listPacks()
				.flatMap(p -> isGroup(p) ? getChildren(p).stream() : Stream.of(p));
	}

	public default Stream<PackResources> getPatchingPacks(ResourceManager manager) {
		return getExpandedPacks(manager).filter(this::hasPatches);
	}

	public default boolean hasPatches(PackResources pack) {
		return pack instanceof IPatchingPackResources ppp && ppp.hasPatches();
	}
}