package net.enderturret.patchedmod.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ReflectionUtil {

	private static final MethodHandle FALLBACKRESOURCEMANAGER_TYPE;

	static {
		try {
			final Field type = ObfuscationReflectionHelper.findField(FallbackResourceManager.class, "f_10601_");
			FALLBACKRESOURCEMANAGER_TYPE = MethodHandles.publicLookup().unreflectGetter(type);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to create accessor for FallbackResourceManager#type (f_10601_):", e);
		}
	}

	public static PackType getType(FallbackResourceManager frm) {
		try {
			return (PackType) FALLBACKRESOURCEMANAGER_TYPE.invoke(frm);
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to access FallbackResourceManager#type (f_10601_):", e);
		}
	}
}