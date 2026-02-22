package net.enderturret.patchedmod.mixin.forge;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.minecraftforge.resource.DelegatingPackResources;

@Mixin(value = DelegatingPackResources.class, remap = false)
public interface DelegatingPackResourcesAccess {

	@Invoker("getCandidatePacks")
	public List<PackResources> patched$getCandidatePacks(PackType type, ResourceLocation location);
}