package net.enderturret.patchedmod.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
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

	static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(Patched.MOD_ID, path);
	}

	/**
	 * Returns the {@code ZipFile} associated with the given pack.
	 * @param pack The pack.
	 * @return The zip file.
	 */
	public static ZipFile getZipFile(FilePackResources pack) {
		return ((SharedZipFileAccessAccess) ((FilePackResourcesAccess) pack).getZipFileAccess()).callGetOrCreateZipFile();
	}

	/**
	 * Creates a {@link ClickEvent} that suggests the specified command.
	 * This is a bridge between ≤1.21.5 and 1.21.6.
	 * @param command The command to suggest.
	 * @return The new {@code ClickEvent}.
	 */
	public static ClickEvent suggestCommand(String command) {
		return new ClickEvent.SuggestCommand(command);
	}

	/**
	 * Creates a {@link HoverEvent} that displays the specified text.
	 * This is a bridge between ≤1.21.5 and 1.21.6.
	 * @param text The text to display.
	 * @return The new {@code HoverEvent}.
	 */
	public static HoverEvent showText(Component text) {
		return new HoverEvent.ShowText(text);
	}

	/**
	 * Provides access to the background executor, for 1.21.1 and 1.21.2.
	 * @param name The name of the executor.
	 * @return The executor.
	 */
	public static Executor getBackgroundExecutor(String name) {
		return Util.backgroundExecutor();
	}

	/**
	 * Retrieves the value with the associated ID from the given registry.
	 * @param <T> The type.
	 * @param registry The registry to retrieve the value from.
	 * @param id The ID of the value to retrieve.
	 * @return The value, or {@code null} if no value corresponds to the given ID.
	 */
	@Nullable
	public static <T> T get(Registry<T> registry, Identifier id) {
		return registry.getValue(id);
	}
}