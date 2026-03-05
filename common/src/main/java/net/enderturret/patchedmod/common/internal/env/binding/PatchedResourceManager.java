package net.enderturret.patchedmod.common.internal.env.binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * Patched's bindings to {@code ResourceManager}.
 * @author EnderTurret
 */
@Internal
@SuppressWarnings("javadoc")
public interface PatchedResourceManager {

	/**
	 * Returns a {@code Stream} over all packs in the given resource manager, expanding {@linkplain PatchedPackResources#patched$getChildren() group packs} as necessary.
	 * @return The stream.
	 */
	public Stream<PatchedPackResources> patched$getExpandedPacks();

	/**
	 * Returns a {@code Stream} over all patching-enabled packs in the given resource manager, expanding {@linkplain PatchedPackResources#patched$getChildren() group packs} as necessary.
	 * This functions like {@link #patched$getExpandedPacks()}, but additionally filtering out non-patching packs.
	 * @return The stream.
	 */
	public Stream<PatchedPackResources> patched$getPatchingPacks();

	/**
	 * Returns whether or not this {@code ResourceManager} is a {@code FallbackResourceManager}.
	 * @return {@code true} if so.
	 */
	public boolean patched$isFallback();

	/**
	 * Returns the number of packs contained in this {@code FallbackResourceManager}.
	 * @return The number of packs.
	 */
	public int patched$getFallbackPackCount();

	/**
	 * Returns the pack at {@code index} in this {@code FallbackResourceManager}.
	 * @param index The index of the desired pack.
	 * @return The pack.
	 */
	public @Nullable PatchedPackResources patched$getFallbackPack(int index);

	// ===== Vanilla methods =====

	public Set<String> patched$getNamespaces();
	public Stream<PatchedPackResources> patched$listPacks();
	public Optional<InputStream> patched$getResource(PatchedResourceLocation location) throws IOException;
	public Optional<List<InputStream>> patched$getResourceStack(PatchedResourceLocation location) throws IOException;
}