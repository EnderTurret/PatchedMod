package net.enderturret.patchedmod.quilt.client;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import net.enderturret.patchedmod.Patched;

/**
 * Various client-side event handlers.
 * @author EnderTurret
 */
@Internal
public final class ClientEvents implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		if (Patched.platform().isModLoaded("quilt_client_command"))
			try {
				PatchedClientCommands.init();
			} catch (Throwable e) {
				Patched.platform().logger().error("Failed to register client commands:", e);
			}
		else
			Patched.platform().logger().info("Not initializing client commands: Quilt Command API not found.");
	}
}