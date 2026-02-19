package net.enderturret.patchedmod.mixin;

import java.util.zip.ZipFile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.packs.FilePackResources.SharedZipFileAccess;

import net.enderturret.patchedmod.internal.VersionedPatchedInternal;

/**
 * See {@link VersionedPatchedInternal#getFileResources}.
 * @author EnderTurret
 */
@Mixin(SharedZipFileAccess.class)
public interface SharedZipFileAccessAccess {

	/**
	 * @return {@link SharedZipFileAccess#getOrCreateZipFile()}.
	 */
	@Invoker(value = "getOrCreateZipFile")
	public ZipFile patched$getOrCreateZipFile();
}