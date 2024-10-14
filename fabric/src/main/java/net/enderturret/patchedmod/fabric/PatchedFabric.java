package net.enderturret.patchedmod.fabric;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.env.IEnvironment;

@Internal
public final class PatchedFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, dedicated) -> {
			dispatcher.register(PatchedCommand.create(new IEnvironment.ServerEnvironment()));
		});
		Patched.setPlatform(new FabricPlatform());
		PatchedTestConditions.registerDefaults();
	}
}