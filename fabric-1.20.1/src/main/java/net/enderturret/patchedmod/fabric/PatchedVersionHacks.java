package net.enderturret.patchedmod.fabric;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.mixin.command.FilePackResourcesAccess;

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
		if (zipFileAccess != null)
			try {
				final Object access = zipFileAccess.invoke(pack);
				return (ZipFile) getOrCreateZipFile.invoke(access);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}

		return ((FilePackResourcesAccess) pack).patched$getOrCreateZipFile();
	}

	private static final @Nullable MethodHandle zipFileAccess;
	private static final @Nullable MethodHandle getOrCreateZipFile;

	static {
		final MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
		MethodHandle tempField = null;
		MethodHandle tempMethod = null;

		if (PatchedPlatform.get().isModLoaded("minecraft", "1.20.2")) {
			final String filePackResourcesInt = "net.minecraft.class_3258";
			final String zipFileAccessInt = "field_45038", zipFileAccessDescInt = "Lnet/minecraft/class_3258/class_8616;";
			final String sharedZipFileAccessInt = "net.minecraft.class_3258.class_8616";
			final String getOrCreateZipFileInt = "method_52426", getOrCreateZipFileDescInt = "()Ljava/util/zip/ZipFile;";
			try {
				final Field zipFileAccess = FilePackResources.class.getDeclaredField(mappings.mapFieldName("intermediary", filePackResourcesInt, zipFileAccessInt, zipFileAccessDescInt));
				zipFileAccess.setAccessible(true);
				tempField = MethodHandles.publicLookup().unreflectGetter(zipFileAccess);

				final Class<?> sharedZipFileAccess = Class.forName(mappings.mapClassName("intermediary", sharedZipFileAccessInt),
						false, PatchedVersionHacks.class.getClassLoader());
				final Method getOrCreateZipFile = sharedZipFileAccess.getDeclaredMethod(mappings.mapMethodName("intermediary", sharedZipFileAccessInt, getOrCreateZipFileInt, getOrCreateZipFileDescInt));
				getOrCreateZipFile.setAccessible(true);
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