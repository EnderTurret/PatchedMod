package net.enderturret.patchedmod.internal.env;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permissions;

import net.enderturret.patchedmod.common.env.PatchedResourceManager;

/**
 * A loader-agnostic implementation of {@code IEnvironment} for the server.
 * @author EnderTurret
 */
@Internal
public final class ServerEnvironment implements IEnvironment<CommandSourceStack> {

	@Override
	public boolean client() {
		return false;
	}

	@Override
	public Class<?> getResourceLocationClass() {
		return Identifier.class;
	}

	@Override
	public ArgumentType<?> getResourceLocationArgumentType() {
		return IdentifierArgument.id();
	}

	@Override
	public PatchedResourceManager getResourceManager(CommandSourceStack source) {
		return (PatchedResourceManager) source.getServer().getResourceManager();
	}

	@Override
	public void sendSuccess(CommandSourceStack source, Component message, boolean allowLogging) {
		source.sendSuccess(() -> message, allowLogging);
	}

	@Override
	public void sendFailure(CommandSourceStack source, Component message) {
		source.sendFailure(message);
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
}