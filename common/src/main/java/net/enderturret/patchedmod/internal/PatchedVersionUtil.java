package net.enderturret.patchedmod.internal;

import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.mixin.FilePackResourcesAccess;

/**
 * Various utilities to reduce the number of Minecraft-version-related differences.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionUtil {

	static ResourceLocation id(String path) {
		return new ResourceLocation(Patched.MOD_ID, path);
	}

	/**
	 * Returns the {@code ZipFile} associated with the given pack.
	 * @param pack The pack.
	 * @return The zip file.
	 */
	public static ZipFile getZipFile(FilePackResources pack) {
		return ((FilePackResourcesAccess) pack).patched$callGetOrCreateZipFile();
	}
}