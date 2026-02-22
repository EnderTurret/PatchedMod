package net.enderturret.patchedmod.fabric.env;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import net.enderturret.patchedmod.common.env.PatchedMutableComponent;
import net.enderturret.patchedmod.common.env.PatchedResourceManager;

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
	public PatchedResourceManager getResourceManager(CommandSourceStack source) {
		return (PatchedResourceManager) source.getServer().getResourceManager();
	}

	@Override
	public void sendSuccess(CommandSourceStack source, boolean allowLogging, PatchedMutableComponent message) {
		final Component msg = ((ComponentWrapper) message).message();
		source.sendSuccess(msg, allowLogging);
	}

	@Override
	public void sendFailure(CommandSourceStack source, PatchedMutableComponent message) {
		source.sendFailure(((ComponentWrapper) message).message());
	}

	@Override
	public boolean hasPermission(CommandSourceStack source, int permissionLevel) {
		if (permissionLevel == 0) return true;
		return source.hasPermission(permissionLevel);
	}
}