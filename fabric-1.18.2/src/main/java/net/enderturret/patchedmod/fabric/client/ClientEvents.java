package net.enderturret.patchedmod.fabric.client;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.api.ClientModInitializer;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;

/**
 * Various client-side event handlers.
 * @author EnderTurret
 */
@Internal
public final class ClientEvents implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		if (PatchedPlatform.get().isModLoaded("fabric-command-api-v1"))
			try {
				PatchedClientCommands.init();
			} catch (Throwable e) {
				PatchedInternal.LOGGER.error("Failed to register client commands:", e);
			}
		else
			PatchedInternal.LOGGER.info("Not initializing client commands: Fabric Command API not found.");
	}
}