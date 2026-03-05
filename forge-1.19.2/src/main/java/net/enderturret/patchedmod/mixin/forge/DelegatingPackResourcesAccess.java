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
 * Provides access to {@link DelegatingPackResources#getCandidatePacks(PackType, ResourceLocation)}.
 * @author EnderTurret
 */
@Mixin(value = DelegatingPackResources.class, remap = false)
public interface DelegatingPackResourcesAccess {

	/**
	 * Provides access to {@link DelegatingPackResources#delegates}.
	 * @return The list of delegate packs.
	 */
	@Accessor("delegates")
	public List<PackResources> patched$delegates();

	/**
	 * Invokes {@link DelegatingPackResources#getCandidatePacks(PackType, ResourceLocation)}.
	 * @param type The pack type.
	 * @param location The location of the file. Only used for the namespace.
	 * @return The list of packs containing the specified namespace.
	 */
	@Invoker("getCandidatePacks")
	public List<PackResources> patched$getCandidatePacks(PackType type, ResourceLocation location);
}