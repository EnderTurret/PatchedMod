package net.enderturret.patchedmod.command;

import static net.enderturret.patchedmod.command.PatchedCommand.translate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.util.IEnvironment;
import net.enderturret.patchedmod.util.IPatchingPackResources;
import net.enderturret.patchedmod.util.PatchUtil;
import net.enderturret.patchedmod.util.PatchingInputStream;

/**
 * Defines the '/patched dump' subcommand, which handles viewing patches and patched files.
 * @author EnderTurret
 */
final class DumpCommand {

	static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		final PackType type = env.client() ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
		return env.literal("dump")
				.then(env.literal("patch")
						.then(env.argument("pack", StringArgumentType.string())
								.suggests((ctx, builder) -> PatchedCommand.suggestPack(ctx, builder, env))
								.then(env.argument("location", ResourceLocationArgument.id())
										.suggests((ctx, builder) -> suggestPatch(ctx, "pack", builder, env))
										.executes(ctx -> dumpPatch(ctx, type, env)))))
				.then(env.literal("file")
						.then(env.argument("location", ResourceLocationArgument.id())
								.suggests((ctx, builder) -> suggestResource(ctx, type, builder, env))
								.executes(ctx -> dumpFile(ctx, env))));
	}

	@SuppressWarnings("resource")
	private static <T> CompletableFuture<Suggestions> suggestPatch(CommandContext<T> ctx, String packArg, SuggestionsBuilder builder, IEnvironment<T> env) {
		final String packName = StringArgumentType.getString(ctx, packArg);
		final String input = builder.getRemaining();
		final ResourceManager man = env.getResourceManager(ctx.getSource());

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

					pack.getResources(type, namespace, "", s -> s.getPath().endsWith(".patch"))
						.stream()
						.filter(loc -> loc.toString().startsWith(input))
						.sorted()
						.map(loc -> loc.getNamespace() + ":" + loc.getPath().substring(1))
						.forEach(builder::suggest);
				}

		return builder.buildFuture();
	}

	private static <T> CompletableFuture<Suggestions> suggestResource(CommandContext<T> ctx, PackType type, SuggestionsBuilder builder, IEnvironment<T> env) {
		final String input = builder.getRemaining();
		final ResourceManager man = env.getResourceManager(ctx.getSource());

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
			pack.getResources(type, reqNamespace, "", s -> s.getPath().endsWith(".json"))
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
	private static <T> int dumpPatch(CommandContext<T> ctx, PackType type, IEnvironment<T> env) {
		final String packName = StringArgumentType.getString(ctx, "pack");
		final ResourceLocation location = ctx.getArgument("location", ResourceLocation.class);
		final ResourceManager man = env.getResourceManager(ctx.getSource());

		final PackResources pack = man.listPacks()
				.filter(p -> packName.equals(p.getName()))
				.findFirst()
				.orElse(null);

		if (pack == null) {
			env.sendFailure(ctx.getSource(), translate("command.patched.dump.pack_not_found", "That pack doesn't exist."));
			return 0;
		}

		if (!pack.hasResource(type, location)) {
			env.sendFailure(ctx.getSource(), translate("command.patched.dump.patch_not_found", "That patch could not be found."));
			return 0;
		}

		try (InputStream is = pack.getResource(type, location)) {
			final String src = PatchUtil.readPrettyJson(is, location.toString() + "(in " + packName + ")", true, true);
			if (src == null) {
				env.sendFailure(ctx.getSource(), translate("command.patched.dump.not_json", "That patch is not a json file. (See console for details.)"));
				return 0;
			}
			env.sendSuccess(ctx.getSource(), Component.literal(src), false);
		} catch (IOException e) {
			Patched.platform().logger().warn("Failed to read resource '{}' from {}:", location, packName, e);
			return 0;
		}

		return Command.SINGLE_SUCCESS;
	}

	private static <T> int dumpFile(CommandContext<T> ctx, IEnvironment<T> env) {
		final ResourceLocation location = ctx.getArgument("location", ResourceLocation.class);
		final ResourceManager man = env.getResourceManager(ctx.getSource());

		final Optional<Resource> op = man.getResource(location);

		if (op.isEmpty()) {
			env.sendFailure(ctx.getSource(), translate("command.patched.dump.file_not_found", "That file could not be found."));
			return 0;
		}

		final Resource res = op.get();

		try (InputStream is = res.open()) {
			final PatchAudit audit = new PatchAudit("null");

			if (is instanceof PatchingInputStream pis)
				pis.withAudit(audit);

			final JsonElement src = PatchUtil.readJson(is, location.toString(), false);

			if (src == null) {
				env.sendFailure(ctx.getSource(), translate("command.patched.dump.not_json", "That file is not a json file."));
				return 0;
			}

			env.sendSuccess(ctx.getSource(), Component.literal(audit.toString(src)), false);
		} catch (NoSuchFileException e) {
			env.sendFailure(ctx.getSource(), translate("command.patched.dump.file_not_found", "That file could not be found."));
			return 0;
		} catch (IOException e) {
			Patched.platform().logger().warn("Failed to read resource '{}':", location, e);
			return 0;
		}

		return Command.SINGLE_SUCCESS;
	}
}