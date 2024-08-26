package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.util.PatchUtil;

/**
 * See {@link PatchUtil#getFileResources}.
 * @author EnderTurret
 */
@Mixin(FilePackResources.class)
public interface FilePackResourcesAccess {

	@Accessor
	public FilePackResources.SharedZipFileAccess getZipFileAccess();
}