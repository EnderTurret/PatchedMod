package net.enderturret.patchedmod.forge;

import java.util.Collection;
import java.util.Collections;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.mixin.command.FilePackResourcesAccess;
import net.enderturret.patchedmod.mixin.forge.DelegatingPackResourcesAccess;

/**
 * Various hacks to make Patched work on newer versions of Minecraft without needing to write entire platform implementations for them.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionHacks {

	public static ZipFile getOrCreateZipFile(FilePackResources pack) {
		return ((FilePackResourcesAccess) pack).patched$getOrCreateZipFile();
	}

	@SuppressWarnings("removal")
	public static Collection<PackResources> getCandidatePacks(PackResources pack, PackType type, String namespace) {
		if (pack instanceof DelegatingPackResourcesAccess access)
			return access.patched$getCandidatePacks(type, new ResourceLocation(namespace, ""));
		return Collections.emptyList();
	}
}