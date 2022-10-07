package net.enderturret.patchedmod.data;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.data.DataGenerator;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.enderturret.patchedmod.Patched;

/**
 * Tests the datagen utilities to verify they actually work.
 * @author EnderTurret
 */
@ApiStatus.Internal
//@EventBusSubscriber(modid = Patched.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class TestDataProvider {

	@SubscribeEvent
	static void gatherData(GatherDataEvent e) {
		e.getGenerator().addProvider(e.includeClient(), new PatchProvider(e.getGenerator(), DataGenerator.Target.RESOURCE_PACK, "patched") {
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
		e.getGenerator().addProvider(e.includeServer(), new PatchProvider(e.getGenerator(), DataGenerator.Target.DATA_PACK, "patched") {
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