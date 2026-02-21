package net.enderturret.patchedmod.mixin.command;

import java.util.zip.ZipFile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.common.internal.PatchedInternal;

/**
 * See {@link PatchedInternal#getFileResources}.
 * @author EnderTurret
 */
@Mixin(FilePackResources.class)
public interface FilePackResourcesAccess {

	/**
	 * @return {@link FilePackResources#getOrCreateZipFile()}.
	 */
	@Invoker(value = "getOrCreateZipFile")
	public ZipFile patched$getOrCreateZipFile();
}