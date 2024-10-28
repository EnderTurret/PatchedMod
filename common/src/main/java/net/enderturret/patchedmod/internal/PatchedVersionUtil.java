package net.enderturret.patchedmod.internal;

import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.mixin.FilePackResourcesAccess;
import net.enderturret.patchedmod.mixin.SharedZipFileAccessAccess;

/**
 * Various utilities to reduce the number of Minecraft-version-related differences.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionUtil {

	static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(Patched.MOD_ID, path);
	}

	public static ZipFile getZipFile(FilePackResources pack) {
		return ((SharedZipFileAccessAccess) ((FilePackResourcesAccess) pack).getZipFileAccess()).callGetOrCreateZipFile();
	}
}