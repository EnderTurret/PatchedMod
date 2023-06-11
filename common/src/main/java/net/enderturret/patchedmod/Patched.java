package net.enderturret.patchedmod;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patchedmod.util.MixinCallbacks;
import net.enderturret.patchedmod.util.PatchUtil;
import net.enderturret.patchedmod.util.env.IPlatform;

/**
 * <p>The main mod class.</p>
 * <p>All the exciting content is in {@link MixinCallbacks} and {@link PatchUtil}.</p>
 * @author EnderTurret
 */
public final class Patched {

	public static final String MOD_ID = "patched";

	private static IPlatform platform;

	private Patched() {}

	@Internal
	public static IPlatform platform() {
		return platform;
	}

	@Internal
	public static void setPlatform(IPlatform value) {
		platform = Objects.requireNonNull(value);
	}

	/**
	 * @deprecated Use {@link PatchUtil#isPatchable(ResourceLocation)} instead.
	 * @param location The location of the file to test.
	 * @return {@code true} if the file at the given location supports being patched, based on its name.
	 */
	@Deprecated(forRemoval = true)
	public static boolean canBePatched(ResourceLocation location) {
		return PatchUtil.isPatchable(location);
	}
}