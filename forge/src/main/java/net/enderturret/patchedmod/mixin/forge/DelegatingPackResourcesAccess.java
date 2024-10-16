package net.enderturret.patchedmod.mixin.forge;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.minecraftforge.resource.DelegatingPackResources;

/**
 * Provides access to some private things in {@link DelegatingPackResources} so that we can handle mod packs correctly.
 * @author EnderTurret
 */
@Mixin(DelegatingPackResources.class)
public interface DelegatingPackResourcesAccess {

	@Invoker(value = "getCandidatePacks", remap = false)
	public List<PackResources> patched$callGetCandidatePacks(PackType type, ResourceLocation location);
}