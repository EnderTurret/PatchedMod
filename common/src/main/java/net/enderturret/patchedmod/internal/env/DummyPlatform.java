package net.enderturret.patchedmod.internal.env;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

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

	private final Logger logger = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return logger;
	}

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

	@Override
	public PackOutput getPackOutput(DataGenerator generator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName(PackResources pack) {
		return pack.packId();
	}

	@Override
	public boolean needsSwapNamespaceAndPath(PackResources pack) {
		return false;
	}

	@Override
	public Function<ResourceLocation, ResourceLocation> getRenamer(PackResources pack, String namespace) {
		return Function.identity();
	}
}