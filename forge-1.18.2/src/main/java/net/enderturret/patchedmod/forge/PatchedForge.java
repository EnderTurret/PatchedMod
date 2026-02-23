package net.enderturret.patchedmod.forge;

import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.common.SingleDataSource;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.command.PatchedCommand;
import net.enderturret.patchedmod.forge.env.ServerEnvironment;

@Internal
@Mod(Patched.MOD_ID)
public final class PatchedForge {

	public PatchedForge() {
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleIMC);
	}

	private void registerCommands(RegisterCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(new ServerEnvironment()));
	}

	private void handleIMC(InterModProcessEvent e) {
		e.getIMCStream().forEachOrdered(message -> {
			switch (message.method()) {
				case "registerDataSource" -> handleDataSource(message.messageSupplier().get(), message.senderModId());
				case "registerTestCondition" -> handleTestCondition(message.messageSupplier().get(), message.senderModId());
				default -> PatchedInternal.LOGGER.warn("Received unknown IMC method {} with content {} from {}!",
						message.method(),
						message.messageSupplier().get(),
						message.senderModId());
			}
		});
	}

	private static void handleDataSource(Object obj, String sender) {
		if (!(obj instanceof Pair<?, ?> pair) || !(pair.getLeft() instanceof ResourceLocation rl) || !(pair.getRight() instanceof BinaryOperator op)) {
			PatchedInternal.LOGGER.warn("Expected Pair<ResourceLocation, BinaryOperator<JsonElement>> from IMC sent by {}, got {}!", sender, obj);
			return;
		}

		Patched.registerDataSource(rl, SingleDataSource.wrap(op));
	}

	private static void handleTestCondition(Object obj, String sender) {
		if (!(obj instanceof Pair<?, ?> pair) || !(pair.getLeft() instanceof ResourceLocation rl) || !(pair.getRight() instanceof Predicate con)) {
			PatchedInternal.LOGGER.warn("Expected Pair<ResourceLocation, Predicate<JsonElement>> from IMC sent by {}, got {}!", sender, obj);
			return;
		}

		Patched.registerSimpleTestCondition(rl, con::test);
	}
}