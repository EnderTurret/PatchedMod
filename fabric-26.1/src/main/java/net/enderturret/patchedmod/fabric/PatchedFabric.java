package net.enderturret.patchedmod.fabric;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.enderturret.patchedmod.common.internal.PatchedInternal;

/**
 * Patched's main mod class.
 * @author EnderTurret
 */
@Internal
public final class PatchedFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		// Use Consumer<Object> here so we get slightly better runtime checks.
		FabricLoader.getInstance().getObjectShare().put("patched:registerDataSource", (Consumer<Object>) obj -> PatchedInternal.handleDataSourceIMC(obj, null));
		FabricLoader.getInstance().getObjectShare().put("patched:registerTestCondition", (Consumer<Object>) obj -> PatchedInternal.handleTestConditionIMC(obj, null));
	}
}