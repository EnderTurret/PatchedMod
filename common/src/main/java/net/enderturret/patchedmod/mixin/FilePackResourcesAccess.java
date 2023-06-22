package net.enderturret.patchedmod.mixin;

import java.util.zip.ZipFile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.util.PatchUtil;

/**
 * See {@link PatchUtil#getFileResources}.
 * @author EnderTurret
 */
@Mixin(FilePackResources.class)
public interface FilePackResourcesAccess {

	@Invoker
	public ZipFile callGetOrCreateZipFile();
}