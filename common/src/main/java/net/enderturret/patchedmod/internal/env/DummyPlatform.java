package net.enderturret.patchedmod.internal.env;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.enderturret.patchedmod.util.env.IPlatform;

/**
 * <p>
 * Represents a "platform" with no loader-specific code.
 * This, of course, means that half the things are nonfunctional, but hopefully good enough to avoid crashing.
 * </p>
 * <p>
 * This is only used in error conditions where e.g. NeoForge hasn't constructed us because someone else blew up.
 * It's immediately swapped out for a loader-specific one (that actually does stuff) whenever the loader actually does its job.
 * </p>
 * @author EnderTurret
 */
@Internal
public final class DummyPlatform implements IPlatform {

	@Override
	public boolean isPhysicalClient() {
		return false;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return false;
	}

	@Override
	public boolean isModLoaded(String modId, String version) {
		return false;
	}
}