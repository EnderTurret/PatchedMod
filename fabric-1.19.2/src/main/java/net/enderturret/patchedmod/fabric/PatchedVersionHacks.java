package net.enderturret.patchedmod.fabric;

import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.mixin.command.FilePackResourcesAccess;

/**
 * Various hacks to make Patched work on newer versions of Minecraft without needing to write entire platform implementations for them.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionHacks {

	public static ZipFile getOrCreateZipFile(FilePackResources pack) {
		return ((FilePackResourcesAccess) pack).patched$getOrCreateZipFile();
	}
}