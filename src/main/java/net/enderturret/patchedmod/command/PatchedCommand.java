package net.enderturret.patchedmod.command;

import static net.minecraft.commands.Commands.literal;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.util.IPatchingPackResources;

public class PatchedCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> create(boolean client, Function<CommandSourceStack,ResourceManager> managerGetter) {
		return literal("patched" + (client ? "c" : ""))
				.requires(src -> src.hasPermission(2))
				.then(DumpCommand.create(client, managerGetter))
				.then(ListCommand.create(managerGetter));
	}

	static CompletableFuture<Suggestions> suggestPack(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, Function<CommandSourceStack,ResourceManager> managerGetter) {
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
}