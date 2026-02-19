package net.enderturret.patchedmod.forge;

import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.Identifier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.common.SingleDataSource;
import net.enderturret.patchedmod.internal.command.PatchedCommand;
import net.enderturret.patchedmod.internal.env.IEnvironment;

@Internal
@Mod(Patched.MOD_ID)
public final class PatchedForge {

	public PatchedForge(IEventBus modBus) {
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		modBus.addListener(this::handleIMC);
		Patched.setPlatform(new ForgePlatform());
	}

	private void registerCommands(RegisterCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(new IEnvironment.ServerEnvironment()));
	}

	private void handleIMC(InterModProcessEvent e) {
		e.getIMCStream().forEachOrdered(message -> {
			switch (message.method()) {
				case "registerDataSource" -> handleDataSource(message.messageSupplier().get(), message.senderModId());
				case "registerTestCondition" -> handleTestCondition(message.messageSupplier().get(), message.senderModId());
				default -> Patched.platform().logger().warn("Received unknown IMC method {} with content {} from {}!",
						message.method(),
						message.messageSupplier().get(),
						message.senderModId());
			}
		});
	}

	private static void handleDataSource(Object obj, String sender) {
		if (!(obj instanceof Pair<?, ?> pair) || !(pair.getLeft() instanceof Identifier rl) || !(pair.getRight() instanceof BinaryOperator op)) {
			Patched.platform().logger().warn("Expected Pair<Identifier, BinaryOperator<JsonElement>> from IMC sent by {}, got {}!", sender, obj);
			return;
		}

		Patched.registerDataSource(rl, SingleDataSource.wrap(op));
	}

	private static void handleTestCondition(Object obj, String sender) {
		if (!(obj instanceof Pair<?, ?> pair) || !(pair.getLeft() instanceof Identifier rl) || !(pair.getRight() instanceof Predicate con)) {
			Patched.platform().logger().warn("Expected Pair<Identifier, Predicate<JsonElement>> from IMC sent by {}, got {}!", sender, obj);
			return;
		}

		Patched.registerSimpleTestCondition(rl, con::test);
	}
}