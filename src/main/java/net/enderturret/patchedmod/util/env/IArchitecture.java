package net.enderturret.patchedmod.util.env;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.util.IPatchingPackResources;

@ApiStatus.Internal
public interface IArchitecture {

	public Logger logger();

	public boolean isPhysicalClient();
	public boolean isModLoaded(String modId);

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
		return pack instanceof IPatchingPackResources ppp && ppp.hasPatches();
	}
}