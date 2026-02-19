package net.enderturret.patchedmod.common.internal.flow;

import java.util.Objects;

import net.enderturret.patchedmod.common.env.PatchedPackResources;

/**
 * An alternative to transforming {@code PackEntry}'s constructor public.
 * @author EnderTurret
 * @param name The name of the pack.
 * @param resources The pack itself.
 */
record Entry(String name, PatchedPackResources resources) {

	Entry {}

	Entry(PatchedPackResources resources) {
		this(Objects.requireNonNull(resources, "resources").patched$getName(), resources);
	}
}