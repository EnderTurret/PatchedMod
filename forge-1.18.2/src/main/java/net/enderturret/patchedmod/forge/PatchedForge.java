package net.enderturret.patchedmod.forge;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.command.PatchedCommand;
import net.enderturret.patchedmod.forge.env.ServerEnvironment;

/**
 * Patched's main mod class.
 * @author EnderTurret
 */
@Internal
@Mod(Patched.MOD_ID)
public final class PatchedForge {

	/**
	 * The main mod constructor.
	 */
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