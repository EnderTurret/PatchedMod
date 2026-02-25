package net.enderturret.patchedmod.mixin.impl;

import java.io.IOException;
import java.io.InputStream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;
import net.enderturret.patchedmod.common.internal.flow.PatchingManager;
import net.enderturret.patchedmod.common.util.PatchUtil;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * This mixin implements the functionality for actually patching resources.
 * @author EnderTurret
 */
@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager {

	@Shadow
	@Final
	private PackType type;

	@WrapOperation(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;getWrappedResource("
					+ "Lnet/minecraft/resources/ResourceLocation;"
					+ "Lnet/minecraft/server/packs/PackResources;"
					+ ")Ljava/io/InputStream;"),
			method = { "getResource" })
	private InputStream patched$replaceResourceMulti(
			FallbackResourceManager _self,
			ResourceLocation location, PackResources pack,
			Operation<InputStream> downstream) throws IOException {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		final InputStream stream = downstream.call(_self, location, pack);
		return patched$chain(stream, (PatchedResourceLocation) location, (PatchedPackResources) pack, false);
	}

	@WrapOperation(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;getWrappedResource("
					+ "Lnet/minecraft/resources/ResourceLocation;"
					+ "Lnet/minecraft/server/packs/PackResources;"
					+ ")Ljava/io/InputStream;"),
			method = { "getResources" })
	private InputStream patched$replaceResourceSinglePack(
			FallbackResourceManager _self,
			ResourceLocation location, PackResources pack,
			Operation<InputStream> downstream) throws IOException {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		final InputStream stream = downstream.call(_self, location, pack);
		return patched$chain(stream, (PatchedResourceLocation) location, (PatchedPackResources) pack, true);
	}

	/**
	 * "Chains" the given {@code InputStream}, returning an {@code InputStream} that patches the data returned by it.
	 * @param delegate The delegate {@code InputStream}.
	 * @param name The location of the data.
	 * @param origin The resource or data pack that the data originated from.
	 * @param singlePack Whether or not only patches from the pack containing the resource should be applied.
	 * @return The new {@code InputStream}.
	 * @throws IOException
	 */
	@Unique
	private InputStream patched$chain(InputStream delegate, PatchedResourceLocation name, PatchedPackResources origin, boolean singlePack) throws IOException {
		if (!PatchUtil.isPatchable(name.patched$getPath())) return delegate;
		final PatchedPackType type = this.type == PackType.CLIENT_RESOURCES ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA;
		final PatchedResourceManager manager = (PatchedResourceManager) this;
		return PatchingManager.newPatchingStream(delegate, manager, origin, type, name, singlePack);
	}
}