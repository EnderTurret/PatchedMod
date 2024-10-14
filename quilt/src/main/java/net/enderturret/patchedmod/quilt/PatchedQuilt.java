package net.enderturret.patchedmod.quilt;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.env.IEnvironment;

@Internal
public final class PatchedQuilt implements ModInitializer {

	@Override
	public void onInitialize(ModContainer mod) {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, dedicated) -> {
			dispatcher.register(PatchedCommand.create(new IEnvironment.ServerEnvironment()));
		});
		Patched.setPlatform(new QuiltPlatform());
		PatchedTestConditions.registerDefaults();
	}
}