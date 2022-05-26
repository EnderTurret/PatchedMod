package net.enderturret.patchedmod.mixin;

import java.io.InputStream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;

import net.enderturret.patchedmod.util.MixinCallbacks;

@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager {

	@Redirect(at = @At(value = "NEW", target = "net/minecraft/server/packs/resources/SimpleResource"), method = { "getResource", "getResources" })
	private SimpleResource replaceResource(String sourceName, ResourceLocation location, InputStream resources, @Nullable InputStream metadata) {
		return MixinCallbacks.loadResource((FallbackResourceManager) (Object) this, sourceName, location, resources, metadata);
	}
}