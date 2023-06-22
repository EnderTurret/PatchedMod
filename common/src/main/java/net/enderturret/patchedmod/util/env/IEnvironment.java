package net.enderturret.patchedmod.util.env;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

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
			source.sendSuccess(message, allowLogging);
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