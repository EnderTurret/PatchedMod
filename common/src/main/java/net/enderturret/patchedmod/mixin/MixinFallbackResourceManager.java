package net.enderturret.patchedmod.mixin;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

import net.enderturret.patchedmod.internal.FallbackResourceManagerHidingTreeMap;
import net.enderturret.patchedmod.internal.MixinCallbacks;

/**
 * <p>This mixin implements the functionality for actually patching resources.</p>
 * <p>This is done by wrapping the {@link IoSupplier}
 * returned by {@link FallbackResourceManager#wrapForDebug(ResourceLocation, PackResources, IoSupplier)} with
 * {@link MixinCallbacks#chain(IoSupplier, FallbackResourceManager, PackType, ResourceLocation, PackResources) MixinCallbacks.chain(IoSupplier, FallbackResourceManager, PackType, ResourceLocation, PackResources)}.</p>
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
			method = { "getResource", "listResourceStacks" })
	private Resource patched$replaceResource(PackResources pack, ResourceLocation location, IoSupplier<InputStream> streamSupplier, IoSupplier<ResourceMetadata> metadataSupplier, Operation<Resource> downstream) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		final IoSupplier<InputStream> sup = MixinCallbacks.chain(streamSupplier, self, type, location, pack);
		return downstream.call(pack, location, sup, metadataSupplier);
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
		return new FallbackResourceManagerHidingTreeMap<>((FallbackResourceManager) (Object) this, type);
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

		final IoSupplier<InputStream> sup = MixinCallbacks.chain(streamSupplier, hidden.manager, hidden.type, location, pack);

		return downstream.call(pack, location, sup, metadataSupplier);
	}
}