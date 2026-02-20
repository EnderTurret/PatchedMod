package net.enderturret.patchedmod.fabric;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;

/**
 * Various hacks to make Patched work on newer versions of Minecraft without needing to write entire platform implementations for them.
 * @author EnderTurret
 */
@Internal
public final class PatchedVersionHacks {

	/**
	 * Creates a {@link ClickEvent} that suggests the specified command.
	 * This is a bridge between ≤1.21.5 and 1.21.6.
	 * @param command The command to suggest.
	 * @return The new {@code ClickEvent}.
	 */
	public static ClickEvent suggestCommand(String command) {
		if (newSuggestCommandClickEvent == null)
			return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);

		try {
			return (ClickEvent) newSuggestCommandClickEvent.invoke(command);
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Creates a {@link HoverEvent} that displays the specified text.
	 * This is a bridge between ≤1.21.5 and 1.21.6.
	 * @param text The text to display.
	 * @return The new {@code HoverEvent}.
	 */
	public static HoverEvent showText(Component text) {
		if (newShowTextHoverEvent == null)
			return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);

		try {
			return (HoverEvent) newShowTextHoverEvent.invoke(text);
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
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

	private static Executor backgroundExecutor;
	private static final @Nullable MethodHandle registryGet;
	private static final @Nullable MethodHandle newSuggestCommandClickEvent;
	private static final @Nullable MethodHandle newShowTextHoverEvent;

	static {
		final MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
		MethodHandle temp = null;

		if (PatchedPlatform.get().isModLoaded("minecraft", "1.21.4")) {
			final String registryInt = "net.minecraft.class_2378";
			final String registryGetValueInt = "method_63535", registryGetValueDescInt = "(Lnet.minecraft.class_2960;)Ljava/lang/Object;";
			final String registryGetValue = mappings.mapMethodName("intermediary", registryInt, registryGetValueInt, registryGetValueDescInt);
			try {
				final Method getValue = Registry.class.getDeclaredMethod(registryGetValue, ResourceLocation.class);
				temp = MethodHandles.publicLookup().unreflect(getValue);
				PatchedInternal.LOGGER.debug("Found Registry.getValue(): {}", getValue);
			} catch (NoSuchMethodException e) {
				PatchedInternal.LOGGER.warn("Could not find Registry.getValue()!", e);
			} catch (Exception e) {
				PatchedInternal.LOGGER.warn("Exception locating Registry.getValue():", e);
			}
		}

		registryGet = temp;
		temp = null;

		final boolean is216 = PatchedPlatform.get().isModLoaded("minecraft", "1.21.6");
		if (is216) {
			final String clickEventSuggestCommandInt = "net.minecraft.class_2558$class_10610";
			try {
				final Class<?> suggestCommand = Class.forName(mappings.mapClassName("intermediary", clickEventSuggestCommandInt),
						true, ClickEvent.class.getClassLoader());
				final Constructor<?> ctor = suggestCommand.getConstructor(String.class);
				temp = MethodHandles.publicLookup().unreflectConstructor(ctor);
				PatchedInternal.LOGGER.debug("Found ClickEvent$SuggestCommand <init>(String): {}", ctor);
			} catch (ClassNotFoundException e) {
				PatchedInternal.LOGGER.warn("Could not find ClickEvent$SuggestCommand!", e);
			} catch (Exception e) {
				PatchedInternal.LOGGER.warn("Exception locating ClickEvent$SuggestCommand <init>(String):", e);
			}
		}

		newSuggestCommandClickEvent = temp;
		temp = null;

		if (is216) {
			final String hoverEventShowTextInt = "net.minecraft.class_2568$class_10613";
			try {
				final Class<?> showText = Class.forName(mappings.mapClassName("intermediary", hoverEventShowTextInt),
						true, HoverEvent.class.getClassLoader());
				final Constructor<?> ctor = showText.getConstructor(Component.class);
				temp = MethodHandles.publicLookup().unreflectConstructor(ctor);
				PatchedInternal.LOGGER.debug("Found HoverEvent$ShowText <init>(Component): {}", ctor);
			} catch (ClassNotFoundException e) {
				PatchedInternal.LOGGER.warn("Could not find HoverEvent$ShowText!", e);
			} catch (Exception e) {
				PatchedInternal.LOGGER.warn("Exception locating HoverEvent$ShowText <init>(Component):", e);
			}
		}

		newShowTextHoverEvent = temp;
	}

	/**
	 * Provides access to the background executor.
	 * This is a bridge between ≤1.21.1, 1.21.2, and ≥1.21.3.
	 * @param name The name of the executor.
	 * @return The executor.
	 */
	public static Executor getBackgroundExecutor(String name) {
		if (backgroundExecutor == null)
			findBackgroundExecutor();

		return backgroundExecutor;
	}

	private static void findBackgroundExecutor() {
		final MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
		final String utilInt = "net.minecraft.class_156";
		final String utilBackgroundExecutorInt = "field_18035";
		String utilBackgroundExecutorDesc = "Ljava/util/concurrent/ExecutorService;";
		String utilBackgroundExecutor = mappings.mapFieldName("intermediary", utilInt, utilBackgroundExecutorInt, utilBackgroundExecutorDesc);
		Field field;

		try {
			field = Util.class.getDeclaredField(utilBackgroundExecutor);
		} catch (NoSuchFieldException e) {
			// Maybe we're actually on 1.21.3. Let's see...
			final String tracingExecutorInt = "net.minecraft.class_10207";
			final String tracingExecutor = mappings.mapClassName("intermediary", tracingExecutorInt);
			utilBackgroundExecutorDesc = "L" + tracingExecutor.replace('.', '/') + ";";
			utilBackgroundExecutor = mappings.mapFieldName("intermediary", utilInt, utilBackgroundExecutorInt, utilBackgroundExecutorDesc);

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