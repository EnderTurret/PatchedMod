package net.enderturret.patchedmod.fabric;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.env.IEnvironment;

@ApiStatus.Internal
public final class PatchedFabric implements ModInitializer {

	// I'm not sure if Fabric provides a less inconvenient way to get the physical side, so here's this.
	@ApiStatus.Internal
	public static boolean physicalClient = false;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, dedicated) -> {
			dispatcher.register(PatchedCommand.create(new IEnvironment.ServerEnvironment()));
		});
		Patched.setArch(new FabricArchitecture());
		PatchedTestConditions.registerDefaults();
	}
}