package net.enderturret.patchedmod.command;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import net.enderturret.patchedmod.util.IEnvironment;
import net.enderturret.patchedmod.util.IPatchingPackResources;

/**
 * Defines the root '/patched' command and provides a few utility methods for the subcommands to use.
 * @author EnderTurret
 */
@ApiStatus.Internal
public final class PatchedCommand {

	@ApiStatus.Internal
	public static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		return env.literal("patched" + (env.client() ? "c" : ""))
				.requires(src -> env.hasPermission(src, 2))
				.then(DumpCommand.create(env))
				.then(ListCommand.create(env));
	}

	static <T> CompletableFuture<Suggestions> suggestPack(CommandContext<T> ctx, SuggestionsBuilder builder, IEnvironment<T> env) {
		final String input = builder.getRemaining();
		final ResourceManager man = env.getResourceManager(ctx.getSource());

		man.listPacks()
			.filter(pack -> pack instanceof IPatchingPackResources patching
					&& patching.hasPatches())
			.map(PackResources::packId)
			.filter(s -> s.startsWith(input))
			.sorted()
			.forEach(builder::suggest);

		return builder.buildFuture();
	}

	static MutableComponent translate(String key, String text, Object... args) {
		// Prefer the translation when running commands on the client.
		return FMLEnvironment.dist == Dist.CLIENT ? Component.translatable(key, args) : Component.literal(text);
	}
}