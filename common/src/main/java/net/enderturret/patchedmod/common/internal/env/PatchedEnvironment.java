package net.enderturret.patchedmod.common.internal.env;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedMutableComponent;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * An abstraction over the client and server command APIs.
 * While Forge and NeoForge have nice {@code CommandSourceStack} abstractions,
 * Fabric and Quilt do not, so we must implement them ourself.
 *
 * @author EnderTurret
 *
 * @param <T> The command source type. This will be {@code CommandSourceStack} on servers, and some other abomination on clients.
 */
@Internal
public interface PatchedEnvironment<T> {

	/**
	 * Returns whether or not the environment represents a client context.
	 * This determines whether to register the client version of the command as well as which pack type to use.
	 * @return {@code true} if this is a client context.
	 */
	public boolean client();

	/**
	 * Returns the {@link PatchedPackType} matching this {@code PatchedEnvironment}.
	 * @return The {@code PatchedPackType}.
	 */
	public default PatchedPackType packType() {
		return client() ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA;
	}

	/**
	 * Returns the {@code ResourceManager} for the specified command source.
	 * @param source The command source.
	 * @return The corresponding resource manager.
	 */
	public PatchedResourceManager getResourceManager(T source);

	/**
	 * Submits a task to execute on the Client or Server Thread.
	 * @param source The command source.
	 * @param task The task to execute.
	 */
	public void submit(T source, Runnable task);

	/**
	 * Triggers a resource reload, for testing.
	 * @param source The command source.
	 * @return A {@code CompletableFuture} representing the operation.
	 */
	public CompletableFuture<Void> reloadResources(T source);

	/**
	 * Returns a new {@link PatchedMutableComponent} based on a {@code TranslatableComponent}.
	 * @param languageKey The translation key, for clients with the mod.
	 * @param message The literal message (in English), for vanilla clients.
	 * @param args Arguments to apply to the message.
	 * @return The new {@code PatchedMutableComponent}.
	 */
	public PatchedMutableComponent translate(String languageKey, String message, Object... args);

	/**
	 * Returns a new {@link PatchedMutableComponent} based on a {@code TextComponent}.
	 * @param text The literal message.
	 * @return The new {@code PatchedMutableComponent}.
	 */
	public PatchedMutableComponent literalText(String text);

	/**
	 * Sends a success message to the command source, and optionally logs the message to operators and the console.
	 * @param source The command source.
	 * @param allowLogging Whether or not to log the message to operators and the server console.
	 * @param languageKey The translation key, for clients with the mod.
	 * @param message The literal message (in English), for vanilla clients.
	 * @param args Arguments to apply to the messages.
	 */
	public default void sendSuccess(T source, boolean allowLogging, @Nullable String languageKey, String message, Object... args) {
		sendSuccess(source, allowLogging, languageKey == null ? literalText(message.formatted(args)) : translate(languageKey, message, args));
	}

	/**
	 * Sends a success message to the command source, and optionally logs the message to operators and the console.
	 * @param source The command source.
	 * @param allowLogging Whether or not to log the message to operators and the server console.
	 * @param message The message.
	 */
	public void sendSuccess(T source, boolean allowLogging, PatchedMutableComponent message);

	/**
	 * Sends an error message to the command source.
	 * @param source The command source.
	 * @param languageKey The translation key, for clients with the mod.
	 * @param message The literal message (in English), for vanilla clients.
	 * @param args Arguments to apply to the messages.
	 */
	public default void sendFailure(T source, @Nullable String languageKey, String message, Object... args) {
		sendFailure(source, languageKey == null ? literalText(message.formatted(args)) : translate(languageKey, message, args));
	}

	/**
	 * Sends an error message to the command source.
	 * @param source The command source.
	 * @param message The message.
	 */
	public void sendFailure(T source, PatchedMutableComponent message);

	/**
	 * Determines whether the command source has the specified permission.
	 * @param source The command source.
	 * @param permissionLevel The permission level.
	 * @return {@code true} if they have the specified permission level.
	 */
	public boolean hasPermission(T source, int permissionLevel);

	/**
	 * Executes the specified command, for testing.
	 * @param source The command source.
	 * @param command The command to execute.
	 * @return The feedback from the command.
	 */
	public String executeCommand(T source, String command);

	/**
	 * Returns the class that represents this platform's {@code ResourceLocation}.
	 * This is necessary because {@code common} needs to grab the {@code PatchedResourceLocation} from the {@code CommandContext}.
	 * @return The {@code ResourceLocation} class.
	 */
	public Class<?> getResourceLocationClass();

	/**
	 * Returns an instance of this platform's {@code ResourceLocationArgument}.
	 * This is necessary so that {@code common} can build commands that take {@code ResourceLocation}s.
	 * @return The {@code ResourceLocation} argument type.
	 */
	public ArgumentType<?> getResourceLocationArgumentType();

	/**
	 * Creates a {@link LiteralArgumentBuilder} for the command source type represented by this environment instance.
	 * @param name The name of the literal.
	 * @return The argument builder.
	 */
	public default LiteralArgumentBuilder<T> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	/**
	 * Creates a {@link RequiredArgumentBuilder} for the command source type represented by this environment instance.
	 * @param <A> The underlying object type of the argument.
	 * @param name The name of the argument.
	 * @param type The argument type.
	 * @return The argument builder.
	 */
	public default <A> RequiredArgumentBuilder<T, A> argument(String name, ArgumentType<A> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}
}