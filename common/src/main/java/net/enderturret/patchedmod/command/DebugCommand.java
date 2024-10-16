package net.enderturret.patchedmod.command;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

import net.enderturret.patchedmod.util.MixinCallbacks;
import net.enderturret.patchedmod.util.PatchTargetManager;
import net.enderturret.patchedmod.util.env.IEnvironment;

final class DebugCommand {

	static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		return env.literal("debug")
				.then(env.literal("dumpTargetManagers")
						.executes(ctx -> dumpTargetManagers(ctx, env)));
	}

	private static <T> int dumpTargetManagers(CommandContext<T> ctx, IEnvironment<T> env) {
		final Map<PackType, PatchTargetManager> managers = MixinCallbacks.getTargetManagers();

		for (PackType type : PackType.values())
			env.sendSuccess(
					ctx.getSource(),
					Component.literal(type.name() + " : " + managers.get(type) + "\n"),
					false);

		return Command.SINGLE_SUCCESS;
	}
}