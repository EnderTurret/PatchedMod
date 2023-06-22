package net.enderturret.patchedmod;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.IEnvironment;
import net.enderturret.patchedmod.util.MixinCallbacks;
import net.enderturret.patchedmod.util.PatchUtil;
import net.enderturret.patchedmod.util.env.IPlatform;

/**
 * <p>The main mod class.</p>
 * <p>All the exciting content is in {@link MixinCallbacks} and {@link PatchUtil}.</p>
 * @author EnderTurret
 */
public class Patched implements ModInitializer {

	public static final String MOD_ID = "patched";

	private static IPlatform platform;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, dedicated) -> {
			dispatcher.register(PatchedCommand.create(new ServerEnvironment()));
		});
	}

	@Internal
	public static IPlatform platform() {
		return platform;
	}

	@Internal
	public static void setPlatform(IPlatform value) {
		platform = Objects.requireNonNull(value);
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