package net.enderturret.patchedmod.quilt.client;

import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.env.IEnvironment;

/**
 * Manages the registration of Patched's client commands on Quilt.
 * This is only possible with Fabric API, since Minecraft doesn't exactly have client commands, and Quilt's appears to have vanished.
 * @author EnderTurret
 */
final class PatchedClientCommands {

	static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
			dispatcher.register(PatchedCommand.create(new ClientEnvironment()));
		});
	}

	private static final class ClientEnvironment implements IEnvironment<QuiltClientCommandSource> {

		@Override
		public boolean client() {
			return true;
		}

		@Override
		public ResourceManager getResourceManager(QuiltClientCommandSource source) {
			return Minecraft.getInstance().getResourceManager();
		}

		@Override
		public void sendSuccess(QuiltClientCommandSource source, Component message, boolean allowLogging) {
			source.sendFeedback(message);
		}

		@Override
		public void sendFailure(QuiltClientCommandSource source, Component message) {
			source.sendError(message);
		}

		@Override
		public boolean hasPermission(QuiltClientCommandSource source, int level) {
			return true;
		}
	}
}