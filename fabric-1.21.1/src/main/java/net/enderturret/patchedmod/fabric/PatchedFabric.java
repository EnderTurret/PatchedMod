package net.enderturret.patchedmod.fabric;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.common.SingleDataSource;
import net.enderturret.patchedmod.common.internal.PatchedInternal;

@Internal
public final class PatchedFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		// Use Consumer<Object> here so we get slightly better runtime checks.
		FabricLoader.getInstance().getObjectShare().put("patched:registerDataSource", (Consumer<Object>) obj -> PatchedInternal.handleDataSourceIMC(obj, null));
		FabricLoader.getInstance().getObjectShare().put("patched:registerTestCondition", (Consumer<Object>) obj -> PatchedInternal.handleTestConditionIMC(obj, null));
	}
}