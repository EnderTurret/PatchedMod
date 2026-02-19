package net.enderturret.patchedmod.forge.data;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.data.PackOutput;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.enderturret.patchedmod.data.PatchProvider;

/**
 * Tests the datagen utilities to verify they actually work.
 * @author EnderTurret
 */
@Internal
//@EventBusSubscriber(modid = Patched.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class TestDataProvider {

	@SubscribeEvent
	static void gatherData(GatherDataEvent e) {
		e.getGenerator().addProvider(true, new PatchProvider(e.getGenerator(), PackOutput.Target.RESOURCE_PACK, "patched") {
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
	}
}