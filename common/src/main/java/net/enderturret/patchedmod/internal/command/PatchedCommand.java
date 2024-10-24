package net.enderturret.patchedmod.internal.command;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.internal.MixinCallbacks;
import net.enderturret.patchedmod.internal.env.IEnvironment;

/**
 * Defines the root '/patched' command and provides a few utility methods for the subcommands to use.
 * @author EnderTurret
 */
@Internal
public final class PatchedCommand {

	@Internal
	public static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		final var ret = env.literal("patched" + (env.client() ? "c" : ""))
				.requires(src -> env.hasPermission(src, 2))
				.then(DumpCommand.create(env))
				.then(ListCommand.create(env));

		return MixinCallbacks.DEBUG ? ret.then(DebugCommand.create(env)) : ret;
	}

	static <T> CompletableFuture<Suggestions> suggestPack(CommandContext<T> ctx, SuggestionsBuilder builder, IEnvironment<T> env, boolean quoted) {
		final String input = builder.getRemaining();
		final ResourceManager man = env.getResourceManager(ctx.getSource());

		Patched.platform().getPatchingPacks(man)
			.map(Patched.platform()::getName)
			.filter(s -> s.startsWith(input))
			.sorted()
			.map(s -> quoted ? StringArgumentType.escapeIfRequired(s) : s)
			.forEach(builder::suggest);

		return builder.buildFuture();
	}

	static MutableComponent translate(String key, String text, Object... args) {
		// Make sure we have a fallback for vanilla clients.
		return Component.translatableWithFallback(key, text.formatted(args), args);
	}

	static Style suggestCommand(String command) {
		return Style.EMPTY
				.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
				.withUnderlined(true);
	}
}