package net.enderturret.patchedmod.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.util.IPatchingPackResources;
import net.enderturret.patchedmod.util.PatchUtil;

/**
 * Defines the '/patched dump' subcommand, which handles viewing patches and patched files.
 * @author EnderTurret
 */
public class DumpCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> create(boolean client, Function<CommandSourceStack,ResourceManager> managerGetter) {
		final PackType type = client ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
		return literal("dump")
				.then(literal("patch")
						.then(argument("pack", StringArgumentType.string())
								.suggests((ctx, builder) -> PatchedCommand.suggestPack(ctx, builder, managerGetter))
								.then(argument("location", ResourceLocationArgument.id())
										.suggests((ctx, builder) -> suggestPatch(ctx, "pack", builder, managerGetter))
										.executes(ctx -> dumpPatch(ctx, type, managerGetter)))))
				.then(literal("file")
						.then(argument("location", ResourceLocationArgument.id())
								.suggests((ctx, builder) -> suggestResource(ctx, type, builder, managerGetter))
								.executes(ctx -> dumpFile(ctx, managerGetter))));
	}

	@SuppressWarnings("resource")
	private static CompletableFuture<Suggestions> suggestPatch(CommandContext<CommandSourceStack> ctx, String packArg, SuggestionsBuilder builder, Function<CommandSourceStack,ResourceManager> managerGetter) {
		final String packName = StringArgumentType.getString(ctx, packArg);
		final String input = builder.getRemaining();
		final ResourceManager man = managerGetter.apply(ctx.getSource());

		final int index = input.indexOf(':');
		final String reqNamespace = index == -1 ? null : input.substring(0, index);

		final PackResources pack = man.listPacks()
				.filter(p -> p instanceof IPatchingPackResources patching
						&& patching.hasPatches()
						&& packName.equals(p.getName()))
				.findFirst().orElse(null);

		if (pack != null)
			for (PackType type : PackType.values())
				for (String namespace : pack.getNamespaces(type)) {
					if (reqNamespace != null && !reqNamespace.equals(namespace))
						continue;

					pack.getResources(type, namespace, "", Integer.MAX_VALUE, s -> s.endsWith(".patch"))
						.stream()
						.filter(loc -> loc.toString().startsWith(input))
						.sorted()
						.map(loc -> loc.getNamespace() + ":" + loc.getPath().substring(1))
						.forEach(builder::suggest);
				}

		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestResource(CommandContext<CommandSourceStack> ctx, PackType type, SuggestionsBuilder builder, Function<CommandSourceStack,ResourceManager> managerGetter) {
		final String input = builder.getRemaining();
		final ResourceManager man = managerGetter.apply(ctx.getSource());

		if (!input.contains(":")) {
			man.getNamespaces().stream()
				.filter(ns -> ns.startsWith(input))
				.sorted()
				.forEach(builder::suggest);
			return builder.buildFuture();
		}

		final int index = input.indexOf(':');
		final String reqNamespace = index == -1 ? null : input.substring(0, index);
		final String path = input.substring(index + 1, input.length());

		// Don't process without a filter.
		// There's a lot of files here, so narrowing them down is a requirement.
		if (reqNamespace == null) return builder.buildFuture();

		final List<PackResources> packs = man.listPacks()
				.filter(p -> p.getNamespaces(type).contains(reqNamespace))
				.toList();

		for (PackResources pack : packs)
			pack.getResources(type, reqNamespace, "", Integer.MAX_VALUE, s -> s.endsWith(".json"))
				.stream()
				.filter(loc -> {
					final String l = loc.getNamespace() + ":" + (loc.getPath().startsWith("/") ? loc.getPath().substring(1) : loc.getPath());
					return l.startsWith(input);
				})
				.map(loc -> {
					String fullPath = loc.toString();
					final int weirdSlashIndex = fullPath.indexOf(":/");
					if (weirdSlashIndex != -1)
						fullPath = fullPath.substring(0, weirdSlashIndex + 1) + fullPath.substring(weirdSlashIndex + 2, fullPath.length());

					final int slashIdx = fullPath.indexOf('/', input.length() + 1);

					return slashIdx == -1 ? fullPath : fullPath.substring(0, slashIdx);
				})
				.distinct()
				.sorted()
				.forEach(builder::suggest);

		return builder.buildFuture();
	}

	@SuppressWarnings("resource")
	private static int dumpPatch(CommandContext<CommandSourceStack> ctx, PackType type, Function<CommandSourceStack,ResourceManager> managerGetter) {
		final String packName = StringArgumentType.getString(ctx, "pack");
		final ResourceLocation location = ResourceLocationArgument.getId(ctx, "location");
		final ResourceManager man = managerGetter.apply(ctx.getSource());

		final PackResources pack = man.listPacks()
				.filter(p -> packName.equals(p.getName()))
				.findFirst()
				.orElse(null);

		if (pack == null) {
			ctx.getSource().sendFailure(new TextComponent("That pack doesn't exist."));
			return 0;
		}

		if (!pack.hasResource(type, location)) {
			ctx.getSource().sendFailure(new TextComponent("That patch could not be found."));
			return 0;
		}

		try (InputStream is = pack.getResource(type, location)) {
			final String src = PatchUtil.readPrettyJson(is, location.toString() + "(in " + packName + ")", true, true);
			if (src == null) {
				ctx.getSource().sendFailure(new TextComponent("That patch is not a json file. (See console for details.)"));
				return 0;
			}
			ctx.getSource().sendSuccess(new TextComponent(src), false);
		} catch (IOException e) {
			Patched.LOGGER.warn("Failed to read resource '{}' from {}:", location, packName, e);
			return 0;
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int dumpFile(CommandContext<CommandSourceStack> ctx, Function<CommandSourceStack,ResourceManager> managerGetter) {
		final ResourceLocation location = ResourceLocationArgument.getId(ctx, "location");
		final ResourceManager man = managerGetter.apply(ctx.getSource());

		if (!man.hasResource(location)) {
			ctx.getSource().sendFailure(new TextComponent("That file could not be found."));
			return 0;
		}

		try (Resource res = man.getResource(location); InputStream is = res.getInputStream()) {
			final String src = PatchUtil.readPrettyJson(is, location.toString(), true, false);
			if (src == null) {
				ctx.getSource().sendFailure(new TextComponent("That file is not a json file."));
				return 0;
			}
			ctx.getSource().sendSuccess(new TextComponent(src), false);
		} catch (NoSuchFileException e) {
			ctx.getSource().sendFailure(new TextComponent("That file could not be found."));
			return 0;
		} catch (IOException e) {
			Patched.LOGGER.warn("Failed to read resource '{}':", location, e);
			return 0;
		}

		return Command.SINGLE_SUCCESS;
	}
}