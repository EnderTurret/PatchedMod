package net.enderturret.patchedmod.fabric.client;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import net.minecraft.client.Minecraft;

import net.enderturret.patchedmod.common.env.PatchedMutableComponent;
import net.enderturret.patchedmod.common.env.PatchedResourceManager;
import net.enderturret.patchedmod.common.internal.command.PatchedCommand;
import net.enderturret.patchedmod.fabric.env.AbstractEnvironment;
import net.enderturret.patchedmod.fabric.env.ComponentWrapper;

/**
 * Manages the registration of Patched's client commands on Fabric.
 * This is only possible with Fabric API, since Minecraft doesn't exactly have client commands.
 * @author EnderTurret
 */
final class PatchedClientCommands {

	static void init() {
		ClientCommandManager.DISPATCHER.register(PatchedCommand.create(new ClientEnvironment()));
	}

	private static final class ClientEnvironment extends AbstractEnvironment<FabricClientCommandSource> {

		@Override
		public boolean client() {
			return true;
		}

		@Override
		public PatchedResourceManager getResourceManager(FabricClientCommandSource source) {
			return (PatchedResourceManager) Minecraft.getInstance().getResourceManager();
		}

		@Override
		public void sendSuccess(FabricClientCommandSource source, boolean allowLogging, PatchedMutableComponent message) {
			source.sendFeedback(((ComponentWrapper) message).message());
		}

		@Override
		public void sendFailure(FabricClientCommandSource source, PatchedMutableComponent message) {
			source.sendError(((ComponentWrapper) message).message());
		}

		@Override
		public boolean hasPermission(FabricClientCommandSource source, int permissionLevel) {
			return true;
		}
	}
}