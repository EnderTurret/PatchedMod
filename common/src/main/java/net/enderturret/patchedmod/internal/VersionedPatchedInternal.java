package net.enderturret.patchedmod.internal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Internal utilities for Patched.
 * @author EnderTurret
 */
@Internal
public final class VersionedPatchedInternal {

	private static boolean fileResourcesHookWorks = true;

	/**
	 * Returns a list of all resources in the provided pack under the given namespace that match the specified filter.
	 * @param pack The pack to look for resources in.
	 * @param type The type of resources to look for. Most important for mods, which may have both kinds.
	 * @param namespace The namespace to look under.
	 * @param filter A filter for filtering out undesired results.
	 * @return The list of resources.
	 */
	public static List<PatchedResourceLocation> getResources(PatchedPackResources pack, PatchedPackType type, String namespace, Predicate<PatchedResourceLocation> filter) {
		if (pack instanceof FilePackResources fpp)
			return fileResourcesHookWorks ? getFileResources(fpp, type, namespace, filter) : List.of();

		final List<PatchedResourceLocation> ret = new ArrayList<>();

		// This one's gonna require some explaining:
		// Basically, we want to look at all resources in the pack.
		// The problem is that Minecraft prevents this by bailing for paths "", ".", etc.
		// However, it doesn't check *namespaces*...
		//
		// So what we do here is swap the namespace and path so that it initially
		// resolves the same directory and then resolves the namespace directory.
		// We must use a dot for VanillaPackResources because otherwise LinkFileSystem throws.
		try {
			final Function<PatchedResourceLocation, PatchedResourceLocation> renamer = pack.patched$getRenamer(namespace);
			final String fakeNamespace;
			final String fakePath;

			if (pack.patched$needsSwapNamespaceAndPath()) {
				// The vanilla pack throws on empty paths.
				fakeNamespace = pack.patched$isVanillaPack() ? "." : "";
				fakePath = namespace;
			} else {
				fakeNamespace = namespace;
				fakePath = "";
			}

			pack.patched$listResources(type, fakeNamespace, fakePath, loc -> {
				if (filter.test(loc)) {
					final PatchedResourceLocation renamed = renamer.apply(loc);

					ret.add(renamed);
				}
			});
		} catch (Exception e) {
			PatchedInternal.LOGGER.error("Exception listing resources:", e);
		}

		return ret;
	}

	/**
	 * This method is a better implementation of
	 * {@code FilePackResources#listResources(PackType, String, String, ResourceOutput)}
	 * that actually works for what we need -- getting all resources under a particular namespace.
	 * @param pack The pack in question.
	 * @param type The pack type.
	 * @param namespace The namespace.
	 * @param filter A filter for deciding which resources to include in the returned list.
	 * @return The list of resources under the given namespace.
	 */
	private static List<PatchedResourceLocation> getFileResources(FilePackResources pack, PatchedPackType type, String namespace, Predicate<PatchedResourceLocation> filter) {
		final ZipFile zip;
		try {
			zip = PatchedVersionUtil.getZipFile(pack);
		} catch (Throwable e) {
			PatchedInternal.LOGGER.error("Accessing FilePackResources ZipFile threw an exception! Listing FilePackResources contents is now disabled. Informational commands for zip packs may not work correctly!", e);
			fileResourcesHookWorks = false;
			return List.of();
		}

		if (zip == null) return List.of();

		final List<PatchedResourceLocation> ret = new ArrayList<>();

		final String root = type.directory + "/" + namespace + "/";

		for (Enumeration<? extends ZipEntry> it = zip.entries(); it.hasMoreElements(); ) {
			final ZipEntry entry = it.nextElement();
			if (entry.isDirectory() || !entry.getName().startsWith(root)) continue;

			final String path = entry.getName().substring(root.length());
			final PatchedResourceLocation loc = PatchedPlatform.get().tryBuild(namespace, path);

			if (filter.test(loc))
				ret.add(loc);
		}

		return ret;
	}
}