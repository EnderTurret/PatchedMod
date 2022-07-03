package net.enderturret.patchedmod.util;

import net.minecraft.network.chat.Component;

public interface ICommandSource<T> {

	public void sendSuccess(T source, Component text, boolean allowLogging);
	public void sendFailure(T source, Component text);

	public boolean hasPermission(T source, int level);
}