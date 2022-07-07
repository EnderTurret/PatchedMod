package net.enderturret.patchedmod.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import static net.enderturret.patchedmod.command.PatchedCommand.translate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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
			source.sendFailure(ctx.getSource(), translate("command.patched.list.pack_not_found", "That pack doesn't exist."));
			return 0;
		}

		if (!(pack instanceof IPatchingPackResources patching) || !patching.hasPatches()) {
			source.sendFailure(ctx.getSource(), translate("command.patched.list.patching_disabled", "That pack doesn't have patches enabled."));
			return 0;
		}

		final List<ResourceLocation> patches = new ArrayList<>();

		for (PackType type : PackType.values())
			for (String namespace : pack.getNamespaces(type))
				patches.addAll(pack.getResources(type, namespace, "", Integer.MAX_VALUE, s -> s.endsWith(".patch")));

		final boolean single = patches.size() == 1;

		final MutableComponent c = translate("command.patched.list.patches." + (single ? "single" : "multi"),
				"There " + (!single ? "are" : "is")
				+ " " + patches.size() + " patch" + (!single ? "es" : "")
				+ " in " + pack.getName() + ":", patches.size(), pack.getName());

		final String command = ctx.getNodes().get(0).getNode().getName();

		for (ResourceLocation loc : patches) {
			final String patch = loc.getNamespace() + ":" + loc.getPath().substring(1);
			c.append("\n  ").append(new TextComponent(patch)
					.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
							"/" + command + " dump patch " + pack.getName() + " " + patch))
							.withUnderlined(true)));
		}

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

		final boolean single = packs.size() == 1;

		final MutableComponent c = translate("command.patched.list.packs." + (single ? "single" : "multi"),
				"There " + (!single ? "are" : "is")
				+ " " + packs.size() + " pack" + (!single ? "s" : "")
				+ " with patching enabled:", packs.size());

		final String command = ctx.getNodes().get(0).getNode().getName();

		for (String pack : packs)
			c.append("\n  ").append(new TextComponent(pack)
					.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
							"/" + command + " list patches " + pack))
							.withUnderlined(true)));

		source.sendSuccess(ctx.getSource(), c, false);

		return Command.SINGLE_SUCCESS;
	}
}