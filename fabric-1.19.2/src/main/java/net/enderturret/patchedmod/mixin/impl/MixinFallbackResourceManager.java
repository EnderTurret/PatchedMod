package net.enderturret.patchedmod.mixin.impl;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;
import net.enderturret.patchedmod.common.internal.flow.PatchingManager;
import net.enderturret.patchedmod.common.util.PatchUtil;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.fabric.FallbackResourceManagerAccess;

/**
 * This mixin implements the functionality for actually patching resources.
 * @author EnderTurret
 */
@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager implements FallbackResourceManagerAccess {

	@Shadow
	@Final
	private PackType type;

	@WrapOperation(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;createResourceGetter("
					+ "Lnet/minecraft/resources/ResourceLocation;"
					+ "Lnet/minecraft/server/packs/PackResources;"
					+ ")Lnet/minecraft/server/packs/resources/Resource$IoSupplier;"),
			method = { "getResource", "listResources" })
	private Resource.IoSupplier<InputStream> patched$replaceResourceMulti(
			FallbackResourceManager _self,
			ResourceLocation location, PackResources pack,
			Operation<Resource.IoSupplier<InputStream>> downstream) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		final Resource.IoSupplier<InputStream> streamSupplier = downstream.call(_self, location, pack);
		return patched$chain(streamSupplier, (PatchedResourceLocation) location, (PatchedPackResources) pack, false);
	}

	/**
	 * "Chains" the given {@code IoSupplier}, returning an {@code IoSupplier} that patches the data returned by it.
	 * @param delegate The delegate {@code IoSupplier}.
	 * @param name The location of the data.
	 * @param origin The resource or data pack that the data originated from.
	 * @param singlePack Whether or not only patches from the pack containing the resource should be applied.
	 * @return The new {@code IoSupplier}.
	 */
	@Override
	public Resource.IoSupplier<InputStream> patched$chain(Resource.IoSupplier<InputStream> delegate, PatchedResourceLocation name, PatchedPackResources origin, boolean singlePack) {
		if (!PatchUtil.isPatchable(name.patched$getPath())) return delegate;
		final PatchedPackType type = PatchedPackType.fromVanilla(PackType.CLIENT_RESOURCES, this.type);
		final PatchedResourceManager manager = (PatchedResourceManager) this;
		return () -> PatchingManager.newPatchingStream(delegate.get(), manager, origin, type, name, singlePack);
	}
}