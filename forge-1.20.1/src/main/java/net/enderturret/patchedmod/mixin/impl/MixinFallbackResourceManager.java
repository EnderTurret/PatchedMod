package net.enderturret.patchedmod.mixin.impl;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;

import net.enderturret.patchedmod.common.internal.FallbackResourceManagerHidingTreeMap;
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
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;createResource("
					+ "Lnet/minecraft/server/packs/PackResources;"
					+ "Lnet/minecraft/resources/ResourceLocation;"
					+ "Lnet/minecraft/server/packs/resources/IoSupplier;"
					+ "Lnet/minecraft/server/packs/resources/IoSupplier;"
					+ ")Lnet/minecraft/server/packs/resources/Resource;"),
			method = { "getResource" })
	private Resource patched$replaceResourceMulti(
			PackResources pack, ResourceLocation location, IoSupplier<InputStream> streamSupplier, IoSupplier<ResourceMetadata> metadataSupplier,
			Operation<Resource> downstream) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		streamSupplier = patched$chain(streamSupplier,
				(PatchedResourceManager) self,
				type == PackType.CLIENT_RESOURCES ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA,
				(PatchedResourceLocation) location, (PatchedPackResources) pack, false);
		return downstream.call(pack, location, streamSupplier, metadataSupplier);
	}

	@WrapOperation(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;createResource("
					+ "Lnet/minecraft/server/packs/PackResources;"
					+ "Lnet/minecraft/resources/ResourceLocation;"
					+ "Lnet/minecraft/server/packs/resources/IoSupplier;"
					+ "Lnet/minecraft/server/packs/resources/IoSupplier;"
					+ ")Lnet/minecraft/server/packs/resources/Resource;"),
			method = { "listResourceStacks" })
	private Resource patched$replaceResourceSingle(
			PackResources pack, ResourceLocation location, IoSupplier<InputStream> streamSupplier, IoSupplier<ResourceMetadata> metadataSupplier,
			Operation<Resource> downstream) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		streamSupplier = patched$chain(streamSupplier,
				(PatchedResourceManager) self,
				type == PackType.CLIENT_RESOURCES ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA,
				(PatchedResourceLocation) location, (PatchedPackResources) pack, true);
		return downstream.call(pack, location, streamSupplier, metadataSupplier);
	}

	@WrapOperation(
			at = @At(value = "NEW", target = "net/minecraft/server/packs/resources/Resource"),
			method = { "getResourceStack" })
	private Resource patched$replaceResourceSingleCtor(
			PackResources pack, IoSupplier<InputStream> streamSupplier, IoSupplier<ResourceMetadata> metadataSupplier,
			Operation<Resource> downstream, ResourceLocation location) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		streamSupplier = patched$chain(streamSupplier,
				(PatchedResourceManager) self,
				type == PackType.CLIENT_RESOURCES ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA,
				(PatchedResourceLocation) location, (PatchedPackResources) pack, true);
		return downstream.call(pack, streamSupplier, metadataSupplier);
	}

	/**
	 * The purpose of this redirect is to hide {@code this} in the {@code TreeMap} that
	 * is later captured by a static lambda, where we need access to {@code this} in.
	 */
	@Redirect(
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newTreeMap()Ljava/util/TreeMap;", remap = false),
			method = "listResources",
			require = 1) // We'll crash and burn later if this fails, so may as well explode earlier.
	private TreeMap<?, ?> patched$hideThisInTreeMap() {
		return new FallbackResourceManagerHidingTreeMap<>((PatchedResourceManager) this,
				type == PackType.CLIENT_RESOURCES ? PatchedPackType.CLIENT_RESOURCES : PatchedPackType.SERVER_DATA);
	}

	@WrapOperation(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;createResource("
					+ "Lnet/minecraft/server/packs/PackResources;"
					+ "Lnet/minecraft/resources/ResourceLocation;"
					+ "Lnet/minecraft/server/packs/resources/IoSupplier;"
					+ "Lnet/minecraft/server/packs/resources/IoSupplier;"
					+ ")Lnet/minecraft/server/packs/resources/Resource;"),
			method = { "lambda$listResources$3", "m_244901_", "method_45293" },
			require = 1,
			remap = false)
	private static Resource patched$intricateReplaceResource(
			PackResources pack, ResourceLocation location, IoSupplier<InputStream> streamSupplier, IoSupplier<ResourceMetadata> metadataSupplier,
			Operation<Resource> downstream, Map map1, Map map2, ResourceLocation key, @Coerce Object value) {
		final FallbackResourceManagerHidingTreeMap hidden;
		// Check map2 first since that's more likely to be the TreeMap.
		if (map2 instanceof FallbackResourceManagerHidingTreeMap m)
			hidden = m;
		// Check this one too just in case parameters were shuffled.
		else if (map1 instanceof FallbackResourceManagerHidingTreeMap m)
			hidden = m;
		else
			throw new IllegalStateException("Neither map is the expected type; did a mixin fail?");

		final IoSupplier<InputStream> sup = patched$chain(streamSupplier,
				hidden.manager, hidden.type,
				(PatchedResourceLocation) location, (PatchedPackResources) pack, false);

		return downstream.call(pack, location, sup, metadataSupplier);
	}

	/**
	 * "Chains" the given {@code IoSupplier}, returning an {@code IoSupplier} that patches the data returned by it.
	 * @param delegate The delegate {@code IoSupplier}.
	 * @param manager The resource manager that the data is from.
	 * @param type The type of pack this data is from.
	 * @param name The location of the data.
	 * @param origin The resource or data pack that the data originated from.
	 * @param singlePack Whether or not only patches from the pack containing the resource should be applied.
	 * @return The new {@code IoSupplier}.
	 */
	@Unique
	private static IoSupplier<InputStream> patched$chain(IoSupplier<InputStream> delegate, PatchedResourceManager manager, PatchedPackType type, PatchedResourceLocation name, PatchedPackResources origin, boolean singlePack) {
		if (!PatchUtil.isPatchable(name.patched$getPath())) return delegate;
		return () -> PatchingManager.newPatchingStream(delegate.get(), manager, origin, type, name, singlePack);
	}
}