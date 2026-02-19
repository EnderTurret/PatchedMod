package net.enderturret.patchedmod.common.env;

import java.util.Set;
import java.util.stream.Stream;

public interface PatchedResourceManager {

	/**
	 * Returns a {@code Stream} over all packs in the given resource manager, expanding {@linkplain IPatchingPackResources#patched$getChildren() group packs} as necessary.
	 * @return The stream.
	 */
	public Stream<IPatchingPackResources> patched$getExpandedPacks();

	/**
	 * Returns a {@code Stream} over all patching-enabled packs in the given resource manager, expanding {@linkplain IPatchingPackResources#patched$getChildren() group packs} as necessary.
	 * This functions like {@link #patched$getExpandedPacks()}, but additionally filtering out non-patching packs.
	 * @return The stream.
	 */
	public Stream<IPatchingPackResources> patched$getPatchingPacks();

	// ===== Vanilla methods =====

	public Set<String> patched$getNamespaces();

	public Stream<IPatchingPackResources> patched$listPacks();
}