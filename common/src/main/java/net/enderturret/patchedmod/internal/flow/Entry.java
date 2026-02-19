package net.enderturret.patchedmod.internal.flow;

import java.util.Objects;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager.PackEntry;

import net.enderturret.patchedmod.common.env.PatchedPackResources;

/**
 * An alternative to transforming {@link PackEntry}'s constructor public.
 * @author EnderTurret
 * @param name The name of the pack.
 * @param resources The pack itself.
 */
record Entry(String name, PatchedPackResources resources) {

	Entry {}

	Entry(PackEntry packEntry) {
		this((PatchedPackResources) Objects.requireNonNull(packEntry.resources(), "packEntry.resources()"));
	}

	Entry(PatchedPackResources resources) {
		this(Objects.requireNonNull(resources, "resources").patched$getName(), resources);
	}
}