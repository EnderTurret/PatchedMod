package net.enderturret.patchedmod.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.util.IPatchingPackResources;

/**
 * Defines the '/patched list' subcommand, which handles providing lists of the packs with patches and the patches in those packs.
 * @author EnderTurret
 */
public class ListCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> create(Function<CommandSourceStack,ResourceManager> managerGetter) {
		return literal("list")
				.then(literal("patches")
						.then(argument("pack", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> PatchedCommand.suggestPack(ctx, builder, managerGetter))
								.executes(ctx -> listPatches(ctx, managerGetter))))
				.then(literal("packs").executes(ctx -> listPacks(ctx, managerGetter)));
	}

	@SuppressWarnings("resource")
	private static int listPatches(CommandContext<CommandSourceStack> ctx, Function<CommandSourceStack,ResourceManager> managerGetter) {
		final String packName = StringArgumentType.getString(ctx, "pack");
		final ResourceManager man = managerGetter.apply(ctx.getSource());

		final PackResources pack = man.listPacks()
				.filter(p -> packName.equals(p.getName()))
				.findFirst()
				.orElse(null);

		if (pack == null) {
			ctx.getSource().sendFailure(new TextComponent("That pack doesn't exist."));
			return 0;
		}

		if (!(pack instanceof IPatchingPackResources patching) || !patching.hasPatches()) {
			ctx.getSource().sendFailure(new TextComponent("That pack doesn't have patches enabled."));
			return 0;
		}

		final List<ResourceLocation> patches = new ArrayList<>();

		for (PackType type : PackType.values())
			for (String namespace : pack.getNamespaces(type))
				patches.addAll(pack.getResources(type, namespace, "", Integer.MAX_VALUE, s -> s.endsWith(".patch")));

		final TextComponent c = new TextComponent("There " + (patches.size() != 1 ? "are" : "is")
				+ " " + patches.size() + " patch" + (patches.size() != 1 ? "es" : "")
				+ " in " + pack.getName() + ":");

		for (ResourceLocation loc : patches)
			c.append("\n  " + loc.getNamespace() + ":" + loc.getPath().substring(1));

		ctx.getSource().sendSuccess(c, false);

		return Command.SINGLE_SUCCESS;
	}

	private static int listPacks(CommandContext<CommandSourceStack> ctx, Function<CommandSourceStack,ResourceManager> managerGetter) {
		final ResourceManager man = managerGetter.apply(ctx.getSource());

		final List<String> packs = man.listPacks()
				.filter(p -> p instanceof IPatchingPackResources patching
						&& patching.hasPatches())
				.map(p -> p.getName())
				.sorted()
				.toList();

		final TextComponent c = new TextComponent("There " + (packs.size() != 1 ? "are" : "is")
				+ " " + packs.size() + " pack" + (packs.size() != 1 ? "s" : "")
				+ " with patching enabled:");

		for (String pack : packs)
			c.append("\n  " + pack);

		ctx.getSource().sendSuccess(c, false);

		return Command.SINGLE_SUCCESS;
	}
}