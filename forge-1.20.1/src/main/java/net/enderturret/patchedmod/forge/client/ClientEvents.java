package net.enderturret.patchedmod.forge.client;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.common.env.PatchedMutableComponent;
import net.enderturret.patchedmod.common.env.PatchedResourceManager;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.command.PatchedCommand;
import net.enderturret.patchedmod.forge.env.AbstractEnvironment;
import net.enderturret.patchedmod.forge.env.ComponentWrapper;

/**
 * Various client-side event handlers.
 * @author EnderTurret
 */
@Internal
@EventBusSubscriber(modid = Patched.MOD_ID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
		public void submit(CommandSourceStack source, Runnable task) {
			Minecraft.getInstance().submitAsync(task);
		}

		@Override
		public CompletableFuture<Void> reloadResources(CommandSourceStack source) {
			return Minecraft.getInstance().reloadResourcePacks();
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

		@Override
		public String executeCommand(CommandSourceStack source, String command) {
			final StringBuilder msg = new StringBuilder();
			try {
				ClientCommandHandler.getDispatcher().execute(command, source.withSource(new CommandSource() {
					@Override public void sendSystemMessage(Component component) { msg.append(component.getString()).append("\n"); }
					@Override public boolean acceptsSuccess() { return true; }
					@Override public boolean acceptsFailure() { return true; }
					@Override public boolean shouldInformAdmins() { return false; }
				}));
			} catch (CommandSyntaxException e) {
				PatchedInternal.LOGGER.error("Exception executing command:", e);
			}
			return msg.toString();
		}
	}
}