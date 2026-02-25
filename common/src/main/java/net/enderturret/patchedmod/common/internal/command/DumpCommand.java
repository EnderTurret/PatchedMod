package net.enderturret.patchedmod.common.internal.command;

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

import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.PatchedEnvironment;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;
import net.enderturret.patchedmod.common.util.PatchingInputStream;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Defines the '/patched dump' subcommand, which handles viewing patches and patched files.
 * @author EnderTurret
 */
final class DumpCommand {

	static <T> LiteralArgumentBuilder<T> create(PatchedEnvironment<T> env) {
		final PatchedPackType type = env.client() ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA;
		return env.literal("dump")
				.then(env.literal("patch")
						.then(env.argument("pack", StringArgumentType.string())
								.suggests((ctx, builder) -> PatchedCommand.suggestPack(ctx, builder, env, true))
								.then(env.argument("location", env.getResourceLocationArgumentType())
										.suggests((ctx, builder) -> suggestPatch(ctx, "pack", builder, env))
										.executes(ctx -> dumpPatch(ctx, type, env)))
								.then(env.literal("dynamic")
										.then(env.argument("patch", StringArgumentType.string())
												.executes(ctx -> dumpLocalPatch(ctx, type, env))))))
				.then(env.literal("file")
						.then(env.argument("location", env.getResourceLocationArgumentType())
								.suggests((ctx, builder) -> suggestResource(ctx, type, builder, env))
								.executes(ctx -> dumpFile(ctx, env, true, true))
								.then(env.literal("raw").executes(ctx -> dumpFile(ctx, env, false, true)))
								.then(env.literal("unpatched").executes(ctx -> dumpFile(ctx, env, false, false)))));
	}

	@SuppressWarnings("resource")
	private static <T> CompletableFuture<Suggestions> suggestPatch(CommandContext<T> ctx, String packArg, SuggestionsBuilder builder, PatchedEnvironment<T> env) {
		final String packName = StringArgumentType.getString(ctx, packArg);
		final String input = builder.getRemaining();
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		final int index = input.indexOf(':');
		final String reqNamespace = index == -1 ? null : input.substring(0, index);

		final PatchedPackResources pack = man.patched$getPatchingPacks()
				.filter(p -> packName.equals(p.patched$getName()))
				.findFirst().orElse(null);

		if (pack != null)
			for (PatchedPackType type : PatchedPackType.values())
				for (String namespace : pack.patched$getNamespaces(type)) {
					if (reqNamespace != null && !reqNamespace.equals(namespace))
						continue;

					PatchedInternal.getResources(pack, type, namespace, s -> s.patched$getPath().endsWith(".patch"))
						.stream()
						.filter(loc -> loc.toString().startsWith(input))
						.sorted()
						.map(Object::toString)
						.forEach(builder::suggest);
				}

		return builder.buildFuture();
	}

	static <T> CompletableFuture<Suggestions> suggestResource(CommandContext<T> ctx, PatchedPackType type, SuggestionsBuilder builder, PatchedEnvironment<T> env) {
		final String input = builder.getRemaining();
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		if (!input.contains(":")) {
			man.patched$getNamespaces().stream()
				.filter(ns -> ns.startsWith(input))
				.sorted()
				.forEach(builder::suggest);
			return builder.buildFuture();
		}

		final int index = input.indexOf(':');
		final String reqNamespace = index == -1 ? null : input.substring(0, index);

		// Don't process without a filter.
		// There's a lot of files here, so narrowing them down is a requirement.
		if (reqNamespace == null) return builder.buildFuture();

		final List<PatchedPackResources> packs = man.patched$listPacks()
				.filter(p -> p.patched$getNamespaces(type).contains(reqNamespace))
				.toList();

		for (PatchedPackResources pack : packs)
			PatchedInternal.getResources(pack, type, reqNamespace, s -> s.patched$getPath().endsWith(".json"))
				.stream()
				.filter(loc -> loc.toString().startsWith(input))
				.map(loc -> {
					final String fullPath = loc.toString();

					final int slashIdx = fullPath.indexOf('/', input.length() + 1);

					return slashIdx == -1 ? fullPath : fullPath.substring(0, slashIdx);
				})
				.distinct()
				.sorted()
				.forEach(builder::suggest);

		return builder.buildFuture();
	}

	@SuppressWarnings("resource")
	private static <T> int dumpPatch(CommandContext<T> ctx, PatchedPackType type, PatchedEnvironment<T> env) {
		final String packName = StringArgumentType.getString(ctx, "pack");
		final PatchedResourceLocation location = (PatchedResourceLocation) ctx.getArgument("location", env.getResourceLocationClass());
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		final List<PatchedPackResources> packs = man.patched$listPacks()
				.filter(p -> packName.equals(p.patched$getName()))
				.toList();

		if (packs.isEmpty()) {
			env.sendFailure(ctx.getSource(), "command.patched.dump.pack_not_found", "That pack doesn't exist.");
			return 0;
		}

		if (packs.size() > 1) {
			env.sendFailure(ctx.getSource(), "command.patched.list.too_many_packs", "There is more than one pack with that name.");
			return 0;
		}

		final PatchedPackResources pack = packs.get(0);

		if (!pack.patched$hasPatches()) {
			env.sendFailure(ctx.getSource(), "command.patched.list.patching_disabled", "That pack doesn't have patches enabled.");
			return 0;
		}

		try {
			InputStream io = pack.patched$getResource(type, location);
			// If we can't find the patch, try flipping the pack type.
			if (io == null) io = pack.patched$getResource(type.toVanilla(PatchedPackType.SERVER_DATA, PatchedPackType.CLIENT_RESOURCES), location);

			if (io == null) {
				env.sendFailure(ctx.getSource(), "command.patched.dump.patch_not_found", "That patch could not be found.");
				return 0;
			}

			try (InputStream is = io) {
				final String src = PatchedInternal.readPrettyJson(is, location.toString() + " (in " + packName + ")", true, true);
				if (src == null) {
					env.sendFailure(ctx.getSource(), "command.patched.dump.not_json", "That patch is not a json file. (See console for details.)");
					return 0;
				}
				env.sendSuccess(ctx.getSource(), false, env.literalText(src));
			}
		} catch (IOException e) {
			PatchedInternal.LOGGER.warn("Failed to read resource '{}' from {}:", location, packName, e);
			return 0;
		}

		return Command.SINGLE_SUCCESS;
	}

	@SuppressWarnings("resource")
	private static <T> int dumpLocalPatch(CommandContext<T> ctx, PatchedPackType type, PatchedEnvironment<T> env) {
		final String packName = StringArgumentType.getString(ctx, "pack");
		final String patchName = ctx.getArgument("patch", String.class);
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		final List<PatchedPackResources> packs = man.patched$listPacks()
				.filter(p -> packName.equals(p.patched$getName()))
				.toList();

		if (packs.isEmpty()) {
			env.sendFailure(ctx.getSource(), "command.patched.dump.pack_not_found", "That pack doesn't exist.");
			return 0;
		}

		if (packs.size() > 1) {
			env.sendFailure(ctx.getSource(), "command.patched.list.too_many_packs", "There is more than one pack with that name.");
			return 0;
		}

		final PatchedPackResources pack = packs.get(0);

		if (!pack.patched$hasPatches()) {
			env.sendFailure(ctx.getSource(), "command.patched.list.patching_disabled", "That pack doesn't have patches enabled.");
			return 0;
		}

		try {
			final InputStream io = pack.patched$getRootResource("patches", patchName + ".json.patch");

			if (io == null) {
				env.sendFailure(ctx.getSource(), "command.patched.dump.patch_not_found", "That patch could not be found.");
				return 0;
			}

			try (InputStream is = io) {
				final String src = PatchedInternal.readPrettyJson(is, patchName + " (in " + packName + ")", true, true);
				if (src == null) {
					env.sendFailure(ctx.getSource(), "command.patched.dump.not_json", "That patch is not a json file. (See console for details.)");
					return 0;
				}
				env.sendSuccess(ctx.getSource(), false, env.literalText(src));
			}
		} catch (IOException e) {
			PatchedInternal.LOGGER.warn("Failed to read resource '{}' from {}:", patchName, packName, e);
			return 0;
		}

		return Command.SINGLE_SUCCESS;
	}

	@SuppressWarnings("deprecation")
	private static <T> int dumpFile(CommandContext<T> ctx, PatchedEnvironment<T> env, boolean useAudit, boolean usePatches) {
		final PatchedResourceLocation location = (PatchedResourceLocation) ctx.getArgument("location", env.getResourceLocationClass());
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		try {
			final Optional<InputStream> op = man.patched$getResource(location);

			if (op.isEmpty()) {
				env.sendFailure(ctx.getSource(), "command.patched.dump.file_not_found", "That file could not be found.");
				return 0;
			}

			try (InputStream is = op.get()) {
				final PatchAudit audit = useAudit ? new PatchAudit("null") : null;

				if (audit != null && is instanceof PatchingInputStream pis)
					pis.withAudit(audit);

				if (!usePatches && is instanceof PatchingInputStream pis)
					pis._disablePatching();

				final JsonElement src = PatchedInternal.readJson(is, location.toString(), false);

				if (src == null) {
					env.sendFailure(ctx.getSource(), "command.patched.dump.not_json", "That file is not a json file.");
					return 0;
				}

				env.sendSuccess(ctx.getSource(), false, env.literalText(audit != null ? audit.toString(src) : PatchedInternal.GSON.toJson(src)));
			}
		} catch (NoSuchFileException e) {
			env.sendFailure(ctx.getSource(), "command.patched.dump.file_not_found", "That file could not be found.");
			return 0;
		} catch (IOException e) {
			PatchedInternal.LOGGER.warn("Failed to read resource '{}':", location, e);
			return 0;
		}

		return Command.SINGLE_SUCCESS;
	}
}