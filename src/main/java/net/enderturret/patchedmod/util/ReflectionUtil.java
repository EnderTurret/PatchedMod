package net.enderturret.patchedmod.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * <p>Cursed reflection to access {@code FallbackResourceManager.type} ({@code f_10601_}).</p>
 * <p>This is necessary because the field is private, the mixin refmap fails to work,
 * and I don't want to spend 10 hours waiting for Gradle because of ATs.</p>
 * @author EnderTurret
 */
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

	/**
	 * @param frm The {@link FallbackResourceManager} to query the type of.
	 * @return The value of the {@code type} field.
	 * @throws IllegalStateException In the unlikely event that something goes horribly wrong and accessing the field throws an exception.
	 */
	public static PackType getType(FallbackResourceManager frm) {
		try {
			return (PackType) FALLBACKRESOURCEMANAGER_TYPE.invoke(frm);
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to access FallbackResourceManager#type (f_10601_):", e);
		}
	}
}