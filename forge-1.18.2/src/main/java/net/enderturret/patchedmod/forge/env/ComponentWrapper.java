package net.enderturret.patchedmod.forge.env;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import net.enderturret.patchedmod.common.env.PatchedMutableComponent;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.forge.PatchedVersionHacks;

public record ComponentWrapper(MutableComponent message) implements PatchedMutableComponent {

	public static MutableComponent translate(String languageKey, String message, Object... args) {
		return PatchedPlatform.get().isPhysicalClient() ? new TranslatableComponent(languageKey, args) : new TextComponent(message.formatted(args));
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
				.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));

		if (hoverText != null)
			style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(hoverText)));

		message.append(new TextComponent(literal).withStyle(style));
		return this;
	}

	@Override
	public PatchedMutableComponent appendWithCommand(String literal, String command) {
		message.append(new TextComponent(literal).withStyle(Style.EMPTY
				.withUnderlined(true)
				.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))));
		return this;
	}

	@Override
	public PatchedMutableComponent appendWithHover(String literal, String text) {
		message.append(new TextComponent(literal).withStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(text)))));
		return this;
	}
}