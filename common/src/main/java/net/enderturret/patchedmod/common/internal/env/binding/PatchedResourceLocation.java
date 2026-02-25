package net.enderturret.patchedmod.common.internal.env.binding;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Patched's bindings to {@code ResourceLocation}.
 * Also known as an {@code Identifier} in modern versions, or in Yarn.
 * @author EnderTurret
 */
@Internal
public interface PatchedResourceLocation {

	/**
	 * Returns the namespace of this {@code ResourceLocation}.
	 * @return The namespace.
	 */
	public String patched$getNamespace();

	/**
	 * Returns the path of this {@code ResourceLocation}.
	 * @return The path.
	 */
	public String patched$getPath();

	/**
	 * Returns a new {@code ResourceLocation} with the same namespace but the specified path.
	 * @param path The path of the desired {@code ResourceLocation}.
	 * @return The new {@code ResourceLocation}.
	 */
	public PatchedResourceLocation patched$withPath(String path);
}