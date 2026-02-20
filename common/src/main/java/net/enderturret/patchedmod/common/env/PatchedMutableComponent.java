package net.enderturret.patchedmod.common.env;

import org.jetbrains.annotations.Nullable;

public interface PatchedMutableComponent {

	public PatchedMutableComponent append(String languageKey, String message, Object... args);
	public PatchedMutableComponent append(String literal);

	public PatchedMutableComponent appendWithCommandHover(String literal, String command, @Nullable String hoverText);
	public PatchedMutableComponent appendWithCommand(String literal, String command);
	public PatchedMutableComponent appendWithHover(String literal, String text);
}