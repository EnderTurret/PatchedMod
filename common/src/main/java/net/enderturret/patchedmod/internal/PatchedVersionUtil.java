package net.enderturret.patchedmod.internal;

import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.mixin.FilePackResourcesAccess;
import net.enderturret.patchedmod.mixin.SharedZipFileAccessAccess;

/**
 * Various utilities to reduce the number of Minecraft-version-related differences.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionUtil {

	/**
	 * Returns the {@code ZipFile} associated with the given pack.
	 * @param pack The pack.
	 * @return The zip file.
	 */
	public static ZipFile getZipFile(FilePackResources pack) {
		return ((SharedZipFileAccessAccess) ((FilePackResourcesAccess) pack).patched$getZipFileAccess()).patched$getOrCreateZipFile();
	}
}