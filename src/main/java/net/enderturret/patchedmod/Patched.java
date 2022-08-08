package net.enderturret.patchedmod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.MixinCallbacks;
import net.enderturret.patchedmod.util.PatchUtil;

/**
 * <p>The main mod class.</p>
 * <p>All the exciting content is in {@link MixinCallbacks} and {@link PatchUtil}.</p>
 * @author EnderTurret
 */
@Mod(Patched.MOD_ID)
public class Patched {

	public static final String MOD_ID = "patched";

	public static final Logger LOGGER = LoggerFactory.getLogger("Patched");

	public Patched() {
		ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (version, network) -> true));
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

		PatchedTestConditions.registerSimple(new ResourceLocation(MOD_ID, "mod_loaded"), value -> ModList.get().isLoaded(assertIsString("mod_loaded", value)));
	}

	private void registerCommands(RegisterCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(false, src -> src.getServer().getResourceManager()));
	}

	private static String assertIsString(String id, JsonElement value) {
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
			throw new PatchingException(id + ": value must be a string");

		return value.getAsString();
	}

	/**
	 * @param location The location of the file to test.
	 * @return {@code true} if the file at the given location supports being patched, based on the name.
	 */
	public static boolean canBePatched(ResourceLocation location) {
		final String path = location.getPath();
		return path.endsWith(".json") || (path.endsWith(".mcmeta") && !path.equals("pack.mcmeta"));
	}
}