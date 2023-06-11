package net.enderturret.patchedmod.forge.data;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.data.PackOutput;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.enderturret.patchedmod.data.PatchProvider;

/**
 * Tests the datagen utilities to verify they actually work.
 * @author EnderTurret
 */
@ApiStatus.Internal
//@EventBusSubscriber(modid = Patched.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class TestDataProvider {

	@SubscribeEvent
	static void gatherData(GatherDataEvent e) {
		e.getGenerator().addProvider(e.includeClient(), new PatchProvider(e.getGenerator(), PackOutput.Target.RESOURCE_PACK, "patched") {
			@Override
			public void registerPatches() {
				patch(id("minecraft", "models/item/poisonous_potato"))
					.compound()
					.test("patched:mod_loaded", "forge")
					.replace("/parent", "minecraft:block/anvil")
					.end();

				patch(id("minecraft", "models/block/amethyst_block"))
					.replace("/textures/all", "minecraft:block/beacon");
			}
		});
		e.getGenerator().addProvider(e.includeServer(), new PatchProvider(e.getGenerator(), PackOutput.Target.DATA_PACK, "patched") {
			@Override
			public void registerPatches() {
				patch(id("minecraft", "recipes/andesite"))
					.add("/ingredients/-", Map.of("item", "minecraft:diamond"));

				patch(id("minecraft", "recipes/ender_eye"))
					.remove("/ingredients/1");
			}
		});
	}
}