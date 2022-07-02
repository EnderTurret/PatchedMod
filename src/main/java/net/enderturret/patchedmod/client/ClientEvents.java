package net.enderturret.patchedmod.client;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.command.PatchedCommand;

/**
 * Various client-side event handlers.
 * @author EnderTurret
 */
@EventBusSubscriber(modid = Patched.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {

	@SubscribeEvent
	static void registerClientCommands(RegisterClientCommandsEvent e) {
		e.getDispatcher().register(PatchedCommand.create(true, ctx -> Minecraft.getInstance().getResourceManager()));
	}
}