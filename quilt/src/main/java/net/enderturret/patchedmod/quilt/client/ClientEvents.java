package net.enderturret.patchedmod.quilt.client;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.quilt.PatchedQuilt;
import net.enderturret.patchedmod.util.env.IEnvironment;

/**
 * Various client-side event handlers.
 * @author EnderTurret
 */
@Internal
public final class ClientEvents implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		PatchedQuilt.physicalClient = true;
		ClientCommandRegistrationCallback.EVENT.register(dispatcher -> {
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