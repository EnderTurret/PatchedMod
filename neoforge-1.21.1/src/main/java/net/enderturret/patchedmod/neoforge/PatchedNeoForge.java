package net.enderturret.patchedmod.neoforge;

import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.common.SingleDataSource;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.command.PatchedCommand;
import net.enderturret.patchedmod.neoforge.env.ServerEnvironment;

@Internal
@Mod(Patched.MOD_ID)
public final class PatchedNeoForge {

	public PatchedNeoForge(IEventBus modBus) {
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		modBus.addListener(this::handleIMC);
	}

	private void registerCommands(RegisterCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(new ServerEnvironment()));
	}

	private void handleIMC(InterModProcessEvent e) {
		e.getIMCStream().forEachOrdered(message -> {
			switch (message.method()) {
				case "registerDataSource" -> PatchedInternal.handleDataSourceIMC(message.messageSupplier().get(), message.senderModId());
				case "registerTestCondition" -> PatchedInternal.handleTestConditionIMC(message.messageSupplier().get(), message.senderModId());
				default -> PatchedInternal.LOGGER.warn("Received unknown IMC method {} with content {} from {}!",
						message.method(),
						message.messageSupplier().get(),
						message.senderModId());
			}
		});
	}
}