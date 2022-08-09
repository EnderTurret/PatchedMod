package net.enderturret.patchedmod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.ICommandSource;
import net.enderturret.patchedmod.util.MixinCallbacks;
import net.enderturret.patchedmod.util.PatchUtil;

/**
 * <p>The main mod class.</p>
 * <p>All the exciting content is in {@link MixinCallbacks} and {@link PatchUtil}.</p>
 * @author EnderTurret
 */
public class Patched implements ModInitializer {

	public static final String MOD_ID = "patched";

	public static final Logger LOGGER = LoggerFactory.getLogger("Patched");

	// I'm not sure if Fabric provides a less inconvenient way to get the physical side, so here's this.
	public static boolean physicalClient = false;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, dedicated) -> {
			dispatcher.register(PatchedCommand.create(false, src -> src.getServer().getResourceManager(), new ServerCommandSource()));
		});

		PatchedTestConditions.registerSimple(new ResourceLocation(MOD_ID, "mod_loaded"), value -> FabricLoader.getInstance().isModLoaded(assertIsString("mod_loaded", value)));
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

	public static <S> LiteralArgumentBuilder<S> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	public static <S,T> RequiredArgumentBuilder<S,T> argument(String name, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}

	private static record ServerCommandSource() implements ICommandSource<CommandSourceStack> {
		@Override
		public void sendSuccess(CommandSourceStack source, Component text, boolean allowLogging) {
			source.sendSuccess(text, allowLogging);
		}
		@Override
		public void sendFailure(CommandSourceStack source, Component text) {
			source.sendFailure(text);
		}
		@Override
		public boolean hasPermission(CommandSourceStack source, int level) {
			return source.hasPermission(level);
		}
	}
}