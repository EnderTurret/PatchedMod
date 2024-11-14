package net.enderturret.patchedmod.internal;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.mixin.FilePackResourcesAccess;
import net.enderturret.patchedmod.mixin.SharedZipFileAccessAccess;

/**
 * Various utilities to reduce the number of Minecraft-version-related differences.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionUtil {

	static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(Patched.MOD_ID, path);
	}

	/**
	 * Returns the {@code ZipFile} associated with the given pack.
	 * @param pack The pack.
	 * @return The zip file.
	 */
	public static ZipFile getZipFile(FilePackResources pack) {
		return ((SharedZipFileAccessAccess) ((FilePackResourcesAccess) pack).getZipFileAccess()).callGetOrCreateZipFile();
	}

	private static Executor backgroundExecutor;

	/**
	 * Provides access to the background executor, for 1.21.1 and 1.21.2.
	 * @param name The name of the executor.
	 * @return The executor.
	 */
	public static Executor getBackgroundExecutor(String name) {
		if (backgroundExecutor == null)
			findBackgroundExecutor();

		return backgroundExecutor;
	}

	private static void findBackgroundExecutor() {
		final String utilInt = "net.minecraft.class_156";
		final String utilBackgroundExecutorMoj = "BACKGROUND_EXECUTOR", utilBackgroundExecutorInt = "field_18035";
		String utilBackgroundExecutorDesc = "Ljava/util/concurrent/ExecutorService;";
		String utilBackgroundExecutor = Patched.platform().remapField(utilBackgroundExecutorMoj, utilInt, utilBackgroundExecutorInt, utilBackgroundExecutorDesc);
		Field field;

		try {
			field = Util.class.getDeclaredField(utilBackgroundExecutor);
		} catch (NoSuchFieldException e) {
			// Maybe we're actually on 1.21.3. Let's see...
			final String tracingExecutorMoj = "net.minecraft.TracingExecutor", tracingExecutorInt = "net.minecraft.class_10207";
			final String tracingExecutor = Patched.platform().remapClass(tracingExecutorMoj, tracingExecutorInt);
			utilBackgroundExecutorDesc = "L" + tracingExecutor.replace('.', '/') + ";";
			utilBackgroundExecutor = Patched.platform().remapField(utilBackgroundExecutorMoj, utilInt, utilBackgroundExecutorInt, utilBackgroundExecutorDesc);

			try {
				field = Util.class.getDeclaredField(utilBackgroundExecutor);
			} catch (NoSuchFieldException e1) {
				final IllegalStateException e2 = new IllegalStateException("Could not access Util.BACKGROUND_EXECUTOR", e);
				e2.addSuppressed(e1);
				throw e2;
			}
		}

		field.setAccessible(true);

		try {
			backgroundExecutor = (Executor) field.get(null);
		} catch (Exception e) {
			throw new RuntimeException("Could not access Util.BACKGROUND_EXECUTOR", e);
		}
	}
}