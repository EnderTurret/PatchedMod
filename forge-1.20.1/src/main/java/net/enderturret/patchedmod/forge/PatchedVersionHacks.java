package net.enderturret.patchedmod.forge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.mixin.command.FilePackResourcesAccess;
import net.enderturret.patchedmod.mixin.forge.DelegatingPackResourcesAccess;

/**
 * Various hacks to make Patched work on newer versions of Minecraft without needing to write entire platform implementations for them.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionHacks {

	/**
	 * Provides access to the specified {@code FilePackResources}'s internal {@code ZipFile}.
	 * This is a bridge between 1.20.1 and 1.20.2.
	 * @param pack The {@code FilePackResources} to retrieve the {@code ZipFile} from.
	 * @return The {@code ZipFile}.
	 */
	public static ZipFile getOrCreateZipFile(FilePackResources pack) {
		if (zipFileAccess != null && getOrCreateZipFile != null)
			try {
				final Object access = zipFileAccess.invoke(pack);
				return (ZipFile) getOrCreateZipFile.invoke(access);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}

		return ((FilePackResourcesAccess) pack).patched$getOrCreateZipFile();
	}

	/**
	 * Provides access to the specified {@code PackResources}'s list of packs for the specified pack type and namespace.
	 * @param pack The pack.
	 * @param type The type.
	 * @param namespace The namespace.
	 * @return The list of candidate packs.
	 */
	@SuppressWarnings("removal")
	public static Collection<PackResources> getCandidatePacks(PackResources pack, PackType type, String namespace) {
		if (pack instanceof DelegatingPackResourcesAccess access)
			return access.patched$getCandidatePacks(type, new ResourceLocation(namespace, ""));
		return Collections.emptyList();
	}

	private static final @Nullable MethodHandle zipFileAccess;
	private static final @Nullable MethodHandle getOrCreateZipFile;

	static {
		MethodHandle tempField = null;
		MethodHandle tempMethod = null;

		if (PatchedPlatform.get().isModLoaded("minecraft", "1.20.2")) {
			try {
				final Field zipFileAccess = ObfuscationReflectionHelper.findField(FilePackResources.class, "f_291183_");
				tempField = MethodHandles.publicLookup().unreflectGetter(zipFileAccess);

				final Class<?> sharedZipFileAccess = Class.forName("net.minecraft.server.packs.FilePackResources$SharedZipFileAccess",
						false, PatchedVersionHacks.class.getClassLoader());
				final Method getOrCreateZipFile = ObfuscationReflectionHelper.findMethod(sharedZipFileAccess, "m_295521_");
				tempMethod = MethodHandles.publicLookup().unreflect(getOrCreateZipFile);
				PatchedInternal.LOGGER.debug("Found SharedZipFileAccess.getOrCreateZipFile(): {}", getOrCreateZipFile);
			} catch (Exception e) {
				PatchedInternal.LOGGER.warn("Exception locating SharedZipFileAccess.getOrCreateZipFile():", e);
			}
		}

		zipFileAccess = tempField;
		getOrCreateZipFile = tempMethod;
	}
}