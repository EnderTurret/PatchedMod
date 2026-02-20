package net.enderturret.patchedmod.neoforge.client;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.common.env.PatchedMutableComponent;
import net.enderturret.patchedmod.common.env.PatchedResourceManager;
import net.enderturret.patchedmod.common.internal.command.PatchedCommand;
import net.enderturret.patchedmod.neoforge.env.AbstractEnvironment;
import net.enderturret.patchedmod.neoforge.env.ComponentWrapper;

/**
 * Various client-side event handlers.
 * @author EnderTurret
 */
@Internal
@EventBusSubscriber(modid = Patched.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

	@SubscribeEvent
	static void registerClientCommands(RegisterClientCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(new ClientEnvironment()));
	}

	private static final class ClientEnvironment extends AbstractEnvironment<CommandSourceStack> {

		@Override
		public boolean client() {
			return true;
		}

		@Override
		public PatchedResourceManager getResourceManager(CommandSourceStack source) {
			return (PatchedResourceManager) Minecraft.getInstance().getResourceManager();
		}

		@Override
		public void sendSuccess(CommandSourceStack source, boolean allowLogging, PatchedMutableComponent message) {
			final Component msg = ((ComponentWrapper) message).message();
			source.sendSuccess(() -> msg, allowLogging);
		}

		@Override
		public void sendFailure(CommandSourceStack source, PatchedMutableComponent message) {
			source.sendFailure(((ComponentWrapper) message).message());
		}

		@Override
		public boolean hasPermission(CommandSourceStack source, int permissionLevel) {
			return true;
		}
	}
}