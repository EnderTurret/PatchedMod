package net.enderturret.patchedmod.neoforge.env;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedMutableComponent;
import net.enderturret.patchedmod.neoforge.PatchedVersionHacks;

/**
 * Implements common bindings to {@code MutableComponent}.
 * @author EnderTurret
 * @param message The wrapped component.
 */
public record ComponentWrapper(MutableComponent message) implements PatchedMutableComponent {

	/**
	 * Returns a new {@link MutableComponent} based on a {@code TranslatableComponent}.
	 * @param languageKey The translation key, for clients with the mod.
	 * @param message The literal message (in English), for vanilla clients.
	 * @param args Arguments to apply to the message.
	 * @return The new {@code MutableComponent}.
	 */
	public static MutableComponent translate(String languageKey, String message, Object... args) {
		return Component.translatableWithFallback(languageKey, message.formatted(args), args);
	}

	@Override
	public PatchedMutableComponent append(String languageKey, String message, Object... args) {
		this.message.append(translate(languageKey, message, args));
		return this;
	}

	@Override
	public PatchedMutableComponent append(String literal) {
		message.append(literal);
		return this;
	}

	@Override
	public PatchedMutableComponent appendWithCommandHover(String literal, String command, @Nullable String hoverText) {
		Style style = Style.EMPTY
				.withUnderlined(true)
				.withClickEvent(PatchedVersionHacks.suggestCommand(command));

		if (hoverText != null)
			style = style.withHoverEvent(PatchedVersionHacks.showText(Component.literal(hoverText)));

		message.append(Component.literal(literal).withStyle(style));
		return this;
	}

	@Override
	public PatchedMutableComponent appendWithCommand(String literal, String command) {
		message.append(Component.literal(literal).withStyle(Style.EMPTY
				.withUnderlined(true)
				.withClickEvent(PatchedVersionHacks.suggestCommand(command))));
		return this;
	}

	@Override
	public PatchedMutableComponent appendWithHover(String literal, String text) {
		message.append(Component.literal(literal).withStyle(Style.EMPTY
				.withHoverEvent(PatchedVersionHacks.showText(Component.literal(text)))));
		return this;
	}
}