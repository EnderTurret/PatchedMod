package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.internal.VersionedPatchedInternal;

/**
 * See {@link VersionedPatchedInternal#getFileResources}.
 * @author EnderTurret
 */
@Mixin(FilePackResources.class)
public interface FilePackResourcesAccess {

	/**
	 * @return {@link FilePackResources#zipFileAccess}.
	 */
	@Accessor(value = "zipFileAccess")
	public FilePackResources.SharedZipFileAccess patched$getZipFileAccess();
}