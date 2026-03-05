package net.enderturret.patchedmod.fabric;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.mixin.command.FilePackResourcesAccess;

/**
 * Various hacks to make Patched work on newer versions of Minecraft without needing to write entire platform implementations for them.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionHacks {

	/**
	 * Provides access to the specified {@code FilePackResources}'s internal {@code ZipFile}.
	 * @param pack The {@code FilePackResources} to retrieve the {@code ZipFile} from.
	 * @return The {@code ZipFile}.
	 */
	public static ZipFile getOrCreateZipFile(FilePackResources pack) {
		try {
			return ((FilePackResourcesAccess) pack).patched$getOrCreateZipFile();
		} catch (IOException e) {
			PatchedInternal.LOGGER.error("Caught IOException from getOrCreateZipFile():", e);
			return null;
		}
	}
}