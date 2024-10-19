package net.enderturret.patchedmod.quilt;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;
import net.enderturret.patchedmod.command.PatchedCommand;

@Internal
public final class PatchedQuilt implements ModInitializer {

	@Override
	public void onInitialize(ModContainer mod) {
		Patched.setPlatform(new QuiltPlatform());
		PatchedTestConditions.registerDefaults();
	}
}