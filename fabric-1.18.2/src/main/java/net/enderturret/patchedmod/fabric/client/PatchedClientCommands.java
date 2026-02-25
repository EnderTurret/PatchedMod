package net.enderturret.patchedmod.fabric.client;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider.ElementSuggestionType;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.command.PatchedCommand;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedMutableComponent;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;
import net.enderturret.patchedmod.fabric.env.AbstractEnvironment;
import net.enderturret.patchedmod.fabric.env.ComponentWrapper;

/**
 * Manages the registration of Patched's client commands on Fabric.
 * This is only possible with Fabric API, since Minecraft doesn't exactly have client commands.
 * @author EnderTurret
 */
final class PatchedClientCommands {

	static void init() {
		ClientCommandManager.DISPATCHER.register(PatchedCommand.create(new ClientEnvironment()));
	}

	private static final class ClientEnvironment extends AbstractEnvironment<FabricClientCommandSource> {

		@Override
		public boolean client() {
			return true;
		}

		@Override
		public void submit(FabricClientCommandSource source, Runnable task) {
			Minecraft.getInstance().tell(task);
		}

		@Override
		public CompletableFuture<Void> reloadResources(FabricClientCommandSource source) {
			return Minecraft.getInstance().reloadResourcePacks();
		}

		@Override
		public PatchedResourceManager getResourceManager(FabricClientCommandSource source) {
			return (PatchedResourceManager) Minecraft.getInstance().getResourceManager();
		}

		@Override
		public void sendSuccess(FabricClientCommandSource source, boolean allowLogging, PatchedMutableComponent message) {
			source.sendFeedback(((ComponentWrapper) message).message());
		}

		@Override
		public void sendFailure(FabricClientCommandSource source, PatchedMutableComponent message) {
			source.sendError(((ComponentWrapper) message).message());
		}

		@Override
		public boolean hasPermission(FabricClientCommandSource source, int permissionLevel) {
			return true;
		}

		@Override
		public String executeCommand(FabricClientCommandSource source, String command) {
			final StringBuilder msg = new StringBuilder();
			try {
				ClientCommandManager.DISPATCHER.execute(command, new FabricClientCommandSource() {
					@Override public void sendFeedback(Component message) { msg.append(message.getString()).append("\n"); }
					@Override public void sendError(Component message) { msg.append(message.getString()).append("\n"); }

					@Override public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> commandContext) { return source.customSuggestion(commandContext); }
					@Override public Collection<String> getAllTeams() { return source.getAllTeams(); }
					@Override public Collection<ResourceLocation> getAvailableSoundEvents() { return source.getAvailableSoundEvents(); }
					@Override public Collection<String> getOnlinePlayerNames() { return source.getOnlinePlayerNames(); }
					@Override public Set<ResourceKey<Level>> levels() { return source.levels(); }
					@Override public RegistryAccess registryAccess() { return source.registryAccess(); }
					@Override public CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> resourceKey, ElementSuggestionType elementSuggestionType, SuggestionsBuilder suggestionsBuilder, CommandContext<?> commandContext) { return source.suggestRegistryElements(resourceKey, elementSuggestionType, suggestionsBuilder, commandContext); }
					@Override public boolean hasPermission(int i) { return source.hasPermission(i); }
					@Override public Minecraft getClient() { return source.getClient(); }
					@Override public LocalPlayer getPlayer() { return source.getPlayer(); }
					@Override public ClientLevel getWorld() { return source.getWorld(); }
					@Override public Stream<ResourceLocation> getRecipeNames() { return source.getRecipeNames(); }
				});
			} catch (CommandSyntaxException e) {
				PatchedInternal.LOGGER.error("Exception executing command:", e);
			}
			return msg.toString();
		}
	}
}