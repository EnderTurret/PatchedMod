package net.enderturret.patchedmod.forge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;

import net.enderturret.patchedmod.util.env.IPlatform;

public final class ForgePlatform implements IPlatform {

	private static final Logger LOGGER = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return LOGGER;
	}

	@Override
	public boolean isPhysicalClient() {
		return FMLEnvironment.dist == Dist.CLIENT;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}
}