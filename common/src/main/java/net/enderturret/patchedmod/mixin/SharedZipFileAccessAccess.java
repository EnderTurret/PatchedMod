package net.enderturret.patchedmod.mixin;

import java.util.zip.ZipFile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.packs.FilePackResources.SharedZipFileAccess;

import net.enderturret.patchedmod.internal.PatchedInternal;

/**
 * See {@link PatchedInternal#getFileResources}.
 * @author EnderTurret
 */
@Mixin(SharedZipFileAccess.class)
public interface SharedZipFileAccessAccess {

	/**
	 * @return {@link SharedZipFileAccess#getOrCreateZipFile()}.
	 */
	@Invoker
	public ZipFile callGetOrCreateZipFile();
}