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
record Entry(String name, PackResources resources) {

	Entry {}

	Entry(PackEntry packEntry) {
		this(Objects.requireNonNull(packEntry.resources(), "packEntry.resources()"));
	}

	Entry(PackResources resources) {
		this(((PatchedPackResources) Objects.requireNonNull(resources, "resources")).patched$getName(), resources);
	}
}