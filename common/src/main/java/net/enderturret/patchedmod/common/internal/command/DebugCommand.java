package net.enderturret.patchedmod.common.internal.command;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.enderturret.patchedmod.common.internal.PatchTargetManager;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.IEnvironment;
import net.enderturret.patchedmod.common.internal.flow.DynamicPatches;
import net.enderturret.patchedmod.common.internal.test.PatchedTestHandler;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

final class DebugCommand {

	static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		return env.literal("debug")
				.then(env.literal("test").executes(ctx -> {
					try {
						PatchedTestHandler.runTest(env, ctx.getSource());
					} catch (Exception e) {
						PatchedInternal.LOGGER.error("Exception running tests:", e);
					}
					return Command.SINGLE_SUCCESS;
				}))
				.then(env.literal("dumpTargetManagers")
						.executes(ctx -> dumpTargetManagers(ctx, env)));
	}

	private static <T> int dumpTargetManagers(CommandContext<T> ctx, IEnvironment<T> env) {
		final Map<PatchedPackType, PatchTargetManager> managers = DynamicPatches.getTargetManagers();

		for (PatchedPackType type : PatchedPackType.values())
			env.sendSuccess(
					ctx.getSource(), false,
					null, "%s : %s\n", type.name(), managers.get(type));

		return Command.SINGLE_SUCCESS;
	}
}