package net.enderturret.patchedmod.command;

import static net.minecraft.commands.Commands.literal;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.util.ICommandSource;
import net.enderturret.patchedmod.util.IPatchingPackResources;

/**
 * Defines the root '/patched' command and provides a few utility methods for the subcommands to use.
 * @author EnderTurret
 */
public class PatchedCommand {

	public static <T> LiteralArgumentBuilder<T> create(boolean client, Function<T,ResourceManager> managerGetter, ICommandSource<T> source) {
		return Patched.<T>literal("patched" + (client ? "c" : ""))
				.requires(src -> source.hasPermission(src, 2))
				.then(DumpCommand.create(client, managerGetter, source))
				.then(ListCommand.create(managerGetter, source));
	}

	static <T> CompletableFuture<Suggestions> suggestPack(CommandContext<T> ctx, SuggestionsBuilder builder, Function<T,ResourceManager> managerGetter) {
		final String input = builder.getRemaining();
		final ResourceManager man = managerGetter.apply(ctx.getSource());

		man.listPacks()
			.filter(pack -> pack instanceof IPatchingPackResources patching
					&& patching.hasPatches())
			.map(PackResources::getName)
			.filter(s -> s.startsWith(input))
			.sorted()
			.forEach(builder::suggest);

		return builder.buildFuture();
	}

	static MutableComponent translate(String key, String text, Object... args) {
		// Prefer the translation when running commands on the client.
		return Patched.physicalClient ? new TranslatableComponent(key, args) : new TextComponent(text);
	}
}