package net.enderturret.patchedmod.client;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.IEnvironment;

/**
 * Various client-side event handlers.
 * @author EnderTurret
 */
@ApiStatus.Internal
@EventBusSubscriber(modid = Patched.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {

	@SubscribeEvent
	static void registerClientCommands(RegisterClientCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(new ClientEnvironment()));
	}

	private static final class ClientEnvironment implements IEnvironment<CommandSourceStack> {

		@Override
		public boolean client() {
			return true;
		}

		@Override
		public ResourceManager getResourceManager(CommandSourceStack source) {
			return Minecraft.getInstance().getResourceManager();
		}

		@Override
		public void sendSuccess(CommandSourceStack source, Component message, boolean allowLogging) {
			source.sendSuccess(message, allowLogging);
		}

		@Override
		public void sendFailure(CommandSourceStack source, Component message) {
			source.sendFailure(message);
		}

		@Override
		public boolean hasPermission(CommandSourceStack source, int level) {
			return source.hasPermission(level);
		}
	}
}