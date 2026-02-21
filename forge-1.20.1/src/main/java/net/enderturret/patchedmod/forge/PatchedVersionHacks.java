package net.enderturret.patchedmod.forge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

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

	static {
		MethodHandle temp = null;

		if (PatchedPlatform.get().isModLoaded("minecraft", "1.21.4")) {
			try {
				final Method getValue = Registry.class.getDeclaredMethod("getValue", ResourceLocation.class);
				temp = MethodHandles.publicLookup().unreflect(getValue);
				PatchedInternal.LOGGER.debug("Found Registry.getValue(): {}", getValue);
			} catch (NoSuchMethodException e) {
				PatchedInternal.LOGGER.warn("Could not find Registry.getValue()!", e);
			} catch (Exception e) {
				PatchedInternal.LOGGER.warn("Exception locating Registry.getValue():", e);
			}
		}
	}
}