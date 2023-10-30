package net.enderturret.patchedmod.forge;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.env.IEnvironment;

@Internal
@Mod(Patched.MOD_ID)
public final class PatchedForge {

	public PatchedForge() {
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		Patched.setPlatform(new ForgePlatform());
		PatchedTestConditions.registerDefaults();
	}

	private void registerCommands(RegisterCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(new IEnvironment.ServerEnvironment()));
	}
}