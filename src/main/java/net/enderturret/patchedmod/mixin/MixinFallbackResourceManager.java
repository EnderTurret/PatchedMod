package net.enderturret.patchedmod.mixin;

import java.io.InputStream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;

import net.enderturret.patchedmod.util.MixinCallbacks;

/**
 * <p>This mixin implements the functionality for actually patching resources.</p>
 * <p>This is done by redirecting the {@code new SimpleResource(...)} invocations to
 * {@link MixinCallbacks#loadResource(FallbackResourceManager, String, ResourceLocation, InputStream, InputStream)}.</p>
 * @author EnderTurret
 */
@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager {

	@Redirect(at = @At(value = "NEW", target = "net/minecraft/server/packs/resources/SimpleResource"), method = { "getResource", "getResources" })
	private SimpleResource replaceResource(String sourceName, ResourceLocation location, InputStream resources, @Nullable InputStream metadata) {
		return MixinCallbacks.loadResource((FallbackResourceManager) (Object) this, sourceName, location, resources, metadata);
	}
}