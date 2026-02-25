package net.enderturret.patchedmod.fabric;

import java.io.InputStream;

import net.minecraft.server.packs.resources.Resource;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;

public interface FallbackResourceManagerAccess {

	public Resource.IoSupplier<InputStream> patched$chain(Resource.IoSupplier<InputStream> delegate, PatchedResourceLocation name, PatchedPackResources origin, boolean singlePack);
}