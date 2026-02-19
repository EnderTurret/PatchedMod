package net.enderturret.patchedmod.common.internal.command;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.enderturret.patchedmod.common.env.PatchedPackResources;
import net.enderturret.patchedmod.common.env.PatchedResourceManager;
import net.enderturret.patchedmod.common.internal.env.IEnvironment;
import net.enderturret.patchedmod.common.internal.flow.PatchingManager;

/**
 * Defines the root '/patched' command and provides a few utility methods for the subcommands to use.
 * @author EnderTurret
 */
@Internal
public final class PatchedCommand {

	/**
	 * Creates the {@code LiteralArgumentBuilder} for the `/patched` and `/patchedc` commands.
	 * @param <T> The command source type.
	 * @param env The environment instance, to handle interpreting the command source.
	 * @return The new argument builder.
	 */
	@Internal
	public static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		final var ret = env.literal("patched" + (env.client() ? "c" : ""))
				.requires(src -> env.hasPermission(src, 2))
				.then(DumpCommand.create(env))
				.then(ListCommand.create(env));

		return PatchingManager.DEBUG ? ret.then(DebugCommand.create(env)) : ret;
	}

	static <T> CompletableFuture<Suggestions> suggestPack(CommandContext<T> ctx, SuggestionsBuilder builder, IEnvironment<T> env, boolean quoted) {
		final String input = builder.getRemaining();
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		man.patched$getPatchingPacks()
			.map(PatchedPackResources::patched$getName)
			.filter(s -> s.startsWith(input))
			.sorted()
			.map(s -> quoted ? StringArgumentType.escapeIfRequired(s) : s)
			.forEach(builder::suggest);

		return builder.buildFuture();
	}
}