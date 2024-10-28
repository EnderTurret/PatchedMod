package net.enderturret.patchedmod;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patched.IDataSource;
import net.enderturret.patchedmod.internal.PatchedDataSource;
import net.enderturret.patchedmod.internal.env.DummyPlatform;
import net.enderturret.patchedmod.internal.flow.PatchingManager;
import net.enderturret.patchedmod.util.PatchUtil;
import net.enderturret.patchedmod.util.env.IPlatform;

/**
 * <p>The main mod class.</p>
 * <p>All the exciting content is in {@link PatchingManager} and {@link PatchUtil}.</p>
 * @author EnderTurret
 */
public final class Patched {

	/**
	 * Patched's mod ID.
	 */
	public static final String MOD_ID = "patched";

	private static IPlatform platform;

	private Patched() {}

	/**
	 * Returns Patched's platform instance.
	 * @return The platform.
	 */
	@Internal
	public static IPlatform platform() {
		if (platform == null) {
			platform = new DummyPlatform();
			platform.logger().error("Constructed dummy platform instance! If you're reading this, Patched was not loaded correctly!");
			// Or someone called this method way too early, but no one would do that, right?
		}

		return platform;
	}

	/**
	 * Sets Patched's platform instance.
	 * @param value The new value.
	 */
	@Internal
	public static void setPlatform(IPlatform value) {
		platform = Objects.requireNonNull(value);
	}

	/**
	 * Registers a new {@linkplain IDataSource data source}.
	 * @param id The name of the data source -- what goes in the {@code type} field.
	 * @param source The data source to register.
	 */
	public static void registerDataSource(ResourceLocation id, SingleDataSource source) {
		PatchedDataSource.register(id.toString(), source);
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