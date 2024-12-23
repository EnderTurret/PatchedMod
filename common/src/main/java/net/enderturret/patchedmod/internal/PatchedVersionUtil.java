package net.enderturret.patchedmod.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.core.Registry;
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
	@Nullable
	private static final MethodHandle registryGet;

	static {
		MethodHandle temp = null;

		if (Patched.platform().isModLoaded("minecraft", "1.21.4")) {
			final String registryInt = "net.minecraft.class_2378";
			final String registryGetValueMoj = "getValue", registryGetValueInt = "method_63535", registryGetValueDescInt = "(Lnet.minecraft.class_2960;)Ljava/lang/Object;";
			final String registryGetValue = Patched.platform().remapMethod(registryGetValueMoj, registryInt, registryGetValueInt, registryGetValueDescInt);
			try {
				final Method getValue = Registry.class.getDeclaredMethod(registryGetValue, ResourceLocation.class);
				temp = MethodHandles.publicLookup().unreflect(getValue);
				Patched.platform().logger().debug("Found Registry.getValue(): {}", getValue);
			} catch (NoSuchMethodException e) {
				Patched.platform().logger().warn("Could not find Registry.getValue()!", e);
			} catch (Exception e) {
				Patched.platform().logger().warn("Exception locating Registry.getValue():", e);
			}
		}

		registryGet = temp;
	}

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

	/**
	 * Retrieves the value with the associated ID from the given registry.
	 * @param <T> The type.
	 * @param registry The registry to retrieve the value from.
	 * @param id The ID of the value to retrieve.
	 * @return The value, or {@code null} if no value corresponds to the given ID.
	 */
	@Nullable
	public static <T> T get(Registry<T> registry, ResourceLocation id) {
		if (registryGet == null)
			return registry.get(id);

		try {
			return (T) registryGet.invoke(registry, id);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
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