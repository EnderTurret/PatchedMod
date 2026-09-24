package net.enderturret.patchedmod.neoforge.env;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedMutableComponent;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;

/**
 * A loader-agnostic implementation of {@code IEnvironment} for the server.
 * @author EnderTurret
 */
@Internal
public final class ServerEnvironment extends AbstractEnvironment<CommandSourceStack> {

	@Override
	public boolean client() {
		return false;
	}

	@Override
	public void submit(CommandSourceStack source, Runnable task) {
		source.getServer().submitAsync(task);
	}

	@Override
	public CompletableFuture<Void> reloadResources(CommandSourceStack source) {
		return source.getServer().reloadResources(source.getServer().getPackRepository().getSelectedIds());
	}

	@Override
	public PatchedResourceManager getResourceManager(CommandSourceStack source) {
		return (PatchedResourceManager) source.getServer().getResourceManager();
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
		if (permissionLevel == 0) return true;
		return source.permissions().hasPermission(switch (permissionLevel) {
			case 1 -> Permissions.COMMANDS_MODERATOR;
			case 2 -> Permissions.COMMANDS_GAMEMASTER;
			case 3 -> Permissions.COMMANDS_ADMIN;
			case 4 -> Permissions.COMMANDS_OWNER;
			default -> throw new IllegalArgumentException();
		});
	}

	@Override
	public String executeCommand(CommandSourceStack source, String command) {
		final StringBuilder msg = new StringBuilder();
		source.getServer().getCommands().performPrefixedCommand(source.withSource(new CommandSource() {
			@Override public void sendSystemMessage(Component component) { msg.append(component.getString()).append("\n"); }
			@Override public boolean acceptsSuccess() { return true; }
			@Override public boolean acceptsFailure() { return true; }
			@Override public boolean shouldInformAdmins() { return false; }
		}), command);
		return msg.toString();
	}
}