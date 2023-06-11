package net.enderturret.patchedmod.forge;

import org.jetbrains.annotations.ApiStatus;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.env.IEnvironment;

@ApiStatus.Internal
@Mod(Patched.MOD_ID)
public final class PatchedForge {

	public PatchedForge() {
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		Patched.setArch(new ForgeArchitecture());
		PatchedTestConditions.registerDefaults();
	}

	private void registerCommands(RegisterCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(new IEnvironment.ServerEnvironment()));
	}
}