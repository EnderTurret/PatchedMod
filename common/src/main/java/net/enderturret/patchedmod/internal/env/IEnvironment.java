package net.enderturret.patchedmod.internal.env;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;

import net.enderturret.patchedmod.common.env.PatchedResourceManager;

/**
 * An abstraction over the client and server command APIs.
 * While Forge and NeoForge have nice {@link CommandSourceStack} abstractions,
 * Fabric and Quilt do not, so we must implement them ourself.
 *
 * @author EnderTurret
 *
 * @param <T> The command source type. This will be {@link CommandSourceStack} on servers, and some other abomination on clients.
 */
@Internal
public interface IEnvironment<T> {

	/**
	 * Returns whether or not the environment represents a client context.
	 * This determines whether to register the client version of the command as well as which pack type to use.
	 * @return {@code true} if this is a client context.
	 */
	public boolean client();

	/**
	 * Returns the {@code ResourceManager} for the specified command source.
	 * @param source The command source.
	 * @return The corresponding resource manager.
	 */
	public PatchedResourceManager getResourceManager(T source);

	/**
	 * Sends a success message to the command source, and optionally logs the message to operators and the console.
	 * @param source The command source.
	 * @param message The message.
	 * @param allowLogging Whether or not to log the message to operators and the server console.
	 */
	public void sendSuccess(T source, Component message, boolean allowLogging);

	/**
	 * Sends an error message to the command source.
	 * @param source The command source.
	 * @param message The message.
	 */
	public void sendFailure(T source, Component message);

	/**
	 * Determines whether the command source has the specified permission.
	 * @param source The command source.
	 * @param permission The permission.
	 * @return {@code true} if they have the specified permission level.
	 */
	public boolean hasPermission(T source, Permission permission);

	/**
	 * Creates a {@link LiteralArgumentBuilder} for the command source type represented by this environment instance.
	 * @param name The name of the literal.
	 * @return The argument builder.
	 */
	public default LiteralArgumentBuilder<T> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	/**
	 * Creates a {@link RequiredArgumentBuilder} for the command source type represented by this environment instance.
	 * @param <A> The underlying object type of the argument.
	 * @param name The name of the argument.
	 * @param type The argument type.
	 * @return The argument builder.
	 */
	public default <A> RequiredArgumentBuilder<T, A> argument(String name, ArgumentType<A> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}

	/**
	 * A loader-agnostic implementation of {@code IEnvironment} for the server.
	 * @author EnderTurret
	 */
	@Internal
	public static final class ServerEnvironment implements IEnvironment<CommandSourceStack> {

		@Override
		public boolean client() {
			return false;
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
		public boolean hasPermission(CommandSourceStack source, Permission permission) {
			return source.permissions().hasPermission(permission);
		}
	}
}