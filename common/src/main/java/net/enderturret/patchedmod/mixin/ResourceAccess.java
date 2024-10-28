package net.enderturret.patchedmod.mixin;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;

@Mixin(Resource.class)
public interface ResourceAccess {

	@Accessor("streamSupplier")
	public IoSupplier<InputStream> patched$getStreamSupplier();

	@Accessor("metadataSupplier")
	public IoSupplier<ResourceMetadata> patched$getMetadataSupplier();
}