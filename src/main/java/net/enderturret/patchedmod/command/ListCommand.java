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

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.util.ICommandSource;
import net.enderturret.patchedmod.util.IPatchingPackResources;

/**
 * Defines the '/patched list' subcommand, which handles providing lists of the packs with patches and the patches in those packs.
 * @author EnderTurret
 */
public class ListCommand {

	public static <T> LiteralArgumentBuilder<T> create(Function<T,ResourceManager> managerGetter, ICommandSource<T> source) {
		return Patched.<T>literal("list")
				.then(Patched.<T>literal("patches")
						.then(Patched.<T, String>argument("pack", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> PatchedCommand.suggestPack(ctx, builder, managerGetter))
								.executes(ctx -> listPatches(ctx, managerGetter, source))))
				.then(Patched.<T>literal("packs").executes(ctx -> listPacks(ctx, managerGetter, source)));
	}

	@SuppressWarnings("resource")
	private static <T> int listPatches(CommandContext<T> ctx, Function<T,ResourceManager> managerGetter, ICommandSource<T> source) {
		final String packName = StringArgumentType.getString(ctx, "pack");
		final ResourceManager man = managerGetter.apply(ctx.getSource());

		final PackResources pack = man.listPacks()
				.filter(p -> packName.equals(p.getName()))
				.findFirst()
				.orElse(null);

		if (pack == null) {
			source.sendFailure(ctx.getSource(), new TextComponent("That pack doesn't exist."));
			return 0;
		}

		if (!(pack instanceof IPatchingPackResources patching) || !patching.hasPatches()) {
			source.sendFailure(ctx.getSource(), new TextComponent("That pack doesn't have patches enabled."));
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

		source.sendSuccess(ctx.getSource(), c, false);

		return Command.SINGLE_SUCCESS;
	}

	private static <T> int listPacks(CommandContext<T> ctx, Function<T,ResourceManager> managerGetter, ICommandSource<T> source) {
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

		source.sendSuccess(ctx.getSource(), c, false);

		return Command.SINGLE_SUCCESS;
	}
}