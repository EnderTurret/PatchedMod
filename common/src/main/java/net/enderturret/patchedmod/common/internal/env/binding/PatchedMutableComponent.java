package net.enderturret.patchedmod.common.internal.env.binding;

import org.jetbrains.annotations.Nullable;

/**
 * Patched's bindings to {@code MutableComponent}.
 * @author EnderTurret
 */
public interface PatchedMutableComponent {

	public PatchedMutableComponent append(String languageKey, String message, Object... args);
	public PatchedMutableComponent append(String literal);

	public PatchedMutableComponent appendWithCommandHover(String literal, String command, @Nullable String hoverText);
	public PatchedMutableComponent appendWithCommand(String literal, String command);
	public PatchedMutableComponent appendWithHover(String literal, String text);
}