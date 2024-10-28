package net.enderturret.patchedmod.mixin;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
import net.enderturret.patchedmod.internal.flow.PatchingManager;

/**
 * This mixin implements the functionality for actually patching resources.
 * @author EnderTurret
 */
@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager {

	@Unique
	private static final String CREATE_RESOURCE = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;createResource("
			+ "Lnet/minecraft/server/packs/PackResources;"
			+ "Lnet/minecraft/resources/ResourceLocation;"
			+ "Lnet/minecraft/server/packs/resources/IoSupplier;"
			+ "Lnet/minecraft/server/packs/resources/IoSupplier;"
			+ ")Lnet/minecraft/server/packs/resources/Resource;";

	@Shadow
	@Final
	private PackType type;

	@WrapOperation(
			at = @At(value = "INVOKE", target = CREATE_RESOURCE),
			method = { "getResource" })
	private Resource patched$replaceResourceMulti(
			PackResources pack, ResourceLocation location, IoSupplier<InputStream> streamSupplier, IoSupplier<ResourceMetadata> metadataSupplier,
			Operation<Resource> downstream) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		final IoSupplier<InputStream> sup = PatchingManager.chain(streamSupplier, self, type, location, pack, false);
		return downstream.call(pack, location, sup, metadataSupplier);
	}

	@WrapOperation(
			at = @At(value = "INVOKE", target = CREATE_RESOURCE),
			method = { "listResourceStacks" })
	private Resource patched$replaceResourceSingle(
			PackResources pack, ResourceLocation location, IoSupplier<InputStream> streamSupplier, IoSupplier<ResourceMetadata> metadataSupplier,
			Operation<Resource> downstream) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		final IoSupplier<InputStream> sup = PatchingManager.chain(streamSupplier, self, type, location, pack, true);
		return downstream.call(pack, location, sup, metadataSupplier);
	}

	// This one might take a little bit of explaining.
	// So every loader in this era has a GroupResourcePack/DelegatingPackResources/whatever.
	// The purpose of these is to contain every mod's pack.
	// This means that every loader must patch this method to expand the group pack.
	// On Forge, we'd be fine just doing like above.
	// On Fabric, we'd be screwed, since they manually 'expand' the pack by adding a bunch of resources to the list.
	// This means that in order for this to work on Fabric we have to *transform the list*.
	// Thus, this injector.
	@Inject(
			at = @At("RETURN"),
			method = { "getResourceStack" })
	private void patched$replaceResourceSingleCtor(ResourceLocation location, CallbackInfoReturnable<List<Resource>> cir) {
		final FallbackResourceManager self = (FallbackResourceManager) (Object) this;
		final List<Resource> resources = Objects.requireNonNull(cir.getReturnValue());
		for (int i = 0; i < resources.size(); i++) {
			final Resource res = resources.get(i);
			final ResourceAccess access = (ResourceAccess) res;
			final IoSupplier<InputStream> sup = PatchingManager.chain(access.patched$getStreamSupplier(), self, type, location, res.source(), true);
			resources.set(i, new Resource(res.source(), sup, access.patched$getMetadataSupplier()));
		}
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
			at = @At(value = "INVOKE", target = CREATE_RESOURCE),
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

		final IoSupplier<InputStream> sup = PatchingManager.chain(streamSupplier, hidden.manager, hidden.type, location, pack, false);

		return downstream.call(pack, location, sup, metadataSupplier);
	}
}