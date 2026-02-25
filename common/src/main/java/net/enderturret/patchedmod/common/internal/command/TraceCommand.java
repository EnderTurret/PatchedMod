package net.enderturret.patchedmod.common.internal.command;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.IEnvironment;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;
import net.enderturret.patchedmod.common.util.PatchTrace;
import net.enderturret.patchedmod.common.util.PatchingInputStream;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

final class TraceCommand {

	static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		final PatchedPackType type = env.client() ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA;
		return env.literal("trace")
				.then(env.argument("file", env.getResourceLocationArgumentType())
								.suggests((ctx, builder) -> DumpCommand.suggestResource(ctx, type, builder, env))
								.executes(ctx -> traceFile(ctx, type, env)));
	}

	private static <T> int traceFile(CommandContext<T> ctx, PatchedPackType type, IEnvironment<T> env) {
		final PatchedResourceLocation location = (PatchedResourceLocation) ctx.getArgument("file", env.getResourceLocationClass());
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		try {
			final Optional<InputStream> op = man.patched$getResource(location);

			if (op.isEmpty()) {
				env.sendFailure(ctx.getSource(), "command.patched.dump.file_not_found", "That file could not be found.");
				return 0;
			}

			try (InputStream is = op.get()) {
				final PatchTrace trace = PatchTrace.newInstance();

				if (is instanceof PatchingInputStream pis)
					pis.withTrace(trace);

				final JsonElement src = PatchedInternal.readJson(is, location.toString(), false);

				if (src == null) {
					env.sendFailure(ctx.getSource(), "command.patched.dump.not_json", "That file is not a json file.");
					return 0;
				}

				final StringBuilder sb = new StringBuilder(location.toString());
				for (String line : trace.lines())
					sb.append("\n").append(line);

				env.sendSuccess(ctx.getSource(), false, env.literalText(sb.toString()));
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