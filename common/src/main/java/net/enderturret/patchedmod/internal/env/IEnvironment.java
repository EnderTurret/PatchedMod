package net.enderturret.patchedmod.internal.env;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

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

	public boolean client();

	public ResourceManager getResourceManager(T source);

	public void sendSuccess(T source, Component message, boolean allowLogging);

	public void sendFailure(T source, Component message);

	public boolean hasPermission(T source, int level);

	public default LiteralArgumentBuilder<T> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	public default <A> RequiredArgumentBuilder<T, A> argument(String name, ArgumentType<A> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}

	@Internal
	public static final class ServerEnvironment implements IEnvironment<CommandSourceStack> {

		@Override
		public boolean client() {
			return false;
		}

		@Override
		public ResourceManager getResourceManager(CommandSourceStack source) {
			return source.getServer().getResourceManager();
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
		public boolean hasPermission(CommandSourceStack source, int level) {
			return source.hasPermission(level);
		}
	}
}