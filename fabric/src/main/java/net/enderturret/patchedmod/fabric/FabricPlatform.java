package net.enderturret.patchedmod.fabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;

import net.enderturret.patchedmod.util.env.IPlatform;

public final class FabricPlatform implements IPlatform {

	private static final Logger LOGGER = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return LOGGER;
	}

	@Override
	public boolean isPhysicalClient() {
		return PatchedFabric.physicalClient;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}
}