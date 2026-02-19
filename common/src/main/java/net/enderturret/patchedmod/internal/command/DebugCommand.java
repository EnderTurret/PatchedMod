package net.enderturret.patchedmod.internal.command;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.network.chat.Component;

import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.internal.PatchTargetManager;
import net.enderturret.patchedmod.internal.env.IEnvironment;
import net.enderturret.patchedmod.internal.flow.DynamicPatches;

final class DebugCommand {

	static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		return env.literal("debug")
				.then(env.literal("dumpTargetManagers")
						.executes(ctx -> dumpTargetManagers(ctx, env)));
	}

	private static <T> int dumpTargetManagers(CommandContext<T> ctx, IEnvironment<T> env) {
		final Map<PatchedPackType, PatchTargetManager> managers = DynamicPatches.getTargetManagers();

		for (PatchedPackType type : PatchedPackType.values())
			env.sendSuccess(
					ctx.getSource(),
					Component.literal(type.name() + " : " + managers.get(type) + "\n"),
					false);

		return Command.SINGLE_SUCCESS;
	}
}