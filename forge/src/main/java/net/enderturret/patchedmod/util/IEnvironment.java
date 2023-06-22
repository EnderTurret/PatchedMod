package net.enderturret.patchedmod.util;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

@ApiStatus.Internal
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
}