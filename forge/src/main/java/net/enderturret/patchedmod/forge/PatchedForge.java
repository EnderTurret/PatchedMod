package net.enderturret.patchedmod.forge;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.PatchedTestConditions;
import net.enderturret.patchedmod.command.PatchedCommand;
import net.enderturret.patchedmod.util.env.IEnvironment;

@Internal
@Mod(Patched.MOD_ID)
public final class PatchedForge {

	@Internal
	public PatchedForge() {
		ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (version, network) -> true));
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		Patched.setPlatform(new ForgePlatform());
		PatchedTestConditions.registerDefaults();
	}

	private void registerCommands(RegisterCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(new IEnvironment.ServerEnvironment()));
	}
}