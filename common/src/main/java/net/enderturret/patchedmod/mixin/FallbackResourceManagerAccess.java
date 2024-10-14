package net.enderturret.patchedmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;

/**
 * Allows accessing {@link FallbackResourceManager#type} without an access transformer/widener.
 * This is necessary because on (Neo)Forge, ATs don't apply if an early loading error occurs (which can cause {@link IllegalAccessError IllegalAccessErrors}).
 * @author EnderTurret
 */
@Mixin(FallbackResourceManager.class)
public interface FallbackResourceManagerAccess {

	@Accessor
	public PackType getType();
}