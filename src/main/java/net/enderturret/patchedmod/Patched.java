package net.enderturret.patchedmod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.fml.common.Mod;

@Mod(Patched.MOD_ID)
public class Patched {

	public static final String MOD_ID = "patched";

	public static final Logger LOGGER = LoggerFactory.getLogger("Patched");

	public Patched() {}

	public static boolean canBePatched(ResourceLocation location) {
		final String path = location.getPath();
		return path.endsWith(".json") || (path.endsWith(".mcmeta") && !path.equals("pack.mcmeta"));
	}
}