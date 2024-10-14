package net.enderturret.patchedmod.fabric;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.api.ModInitializer;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;

@Internal
public final class PatchedFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		Patched.setPlatform(new FabricPlatform());
		PatchedTestConditions.registerDefaults();
	}
}