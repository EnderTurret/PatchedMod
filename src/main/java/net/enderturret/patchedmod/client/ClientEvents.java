package net.enderturret.patchedmod.client;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.IEnvironment;

/**
 * Various client-side event handlers.
 * @author EnderTurret
 */
@ApiStatus.Internal
public final class ClientEvents implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		Patched.physicalClient = true;
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
		public void sendSuccess(FabricClientCommandSource source, Component text, boolean allowLogging) {
			source.sendFeedback(text);
		}

		@Override
		public void sendFailure(FabricClientCommandSource source, Component text) {
			source.sendError(text);
		}

		@Override
		public boolean hasPermission(FabricClientCommandSource source, int level) {
			return true;
		}
	}
}