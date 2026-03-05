package net.enderturret.patchedmod.fabric;

import java.io.InputStream;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.server.packs.resources.Resource;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;

/**
 * Provides access to a method exposed by Patched's {@code FallbackResourceManager} mixin, so that it can be used in another mixin.
 * @author EnderTurret
 */
@Internal
public interface FallbackResourceManagerAccess {

	/**
	 * "Chains" the given {@code IoSupplier}, returning an {@code IoSupplier} that patches the data returned by it.
	 * @param delegate The delegate {@code IoSupplier}.
	 * @param name The location of the data.
	 * @param origin The resource or data pack that the data originated from.
	 * @param singlePack Whether or not only patches from the pack containing the resource should be applied.
	 * @return The new {@code IoSupplier}.
	 */
	public Resource.IoSupplier<InputStream> patched$chain(Resource.IoSupplier<InputStream> delegate, PatchedResourceLocation name, PatchedPackResources origin, boolean singlePack);
}