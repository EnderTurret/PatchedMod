package net.enderturret.patchedmod.fabric.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.internal.command.PatchedCommand;
import net.enderturret.patchedmod.internal.env.IEnvironment;

/**
 * Manages the registration of Patched's client commands on Fabric.
 * This is only possible with Fabric API, since Minecraft doesn't exactly have client commands.
 * @author EnderTurret
 */
final class PatchedClientCommands {

	static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
			dispatcher.register(PatchedCommand.create(new ClientEnvironment()));
		});
	}

	private static final class ClientEnvironment implements IEnvironment<FabricClientCommandSource> {

		@Override
		public boolean client() {
			return true;
		}

		@Override
		public ResourceManager getResourceManager(FabricClientCommandSource source) {
			return Minecraft.getInstance().getResourceManager();
		}

		@Override
		public void sendSuccess(FabricClientCommandSource source, Component message, boolean allowLogging) {
			source.sendFeedback(message);
		}

		@Override
		public void sendFailure(FabricClientCommandSource source, Component message) {
			source.sendError(message);
		}

		@Override
		public boolean hasPermission(FabricClientCommandSource source, int level) {
			return true;
		}
	}
}