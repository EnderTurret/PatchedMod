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
	}
}