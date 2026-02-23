package net.enderturret.patchedmod.common.internal.flow;

import java.util.Objects;

import net.enderturret.patchedmod.common.env.PatchedPackResources;

/**
 * An alternative to transforming {@code PackEntry}'s constructor public.
 * @author EnderTurret
 */
final class Entry {

	private final String name;
	private final PatchedPackResources resources;

	/**
	 * Constructs a new {@code Entry}.
	 * @param name The name of the pack.
	 * @param resources The pack itself.
	 */
	public Entry(String name, PatchedPackResources resources) {
		this.name = Objects.requireNonNull(name, "name");
		this.resources = Objects.requireNonNull(resources, "resources");
	}

	public Entry(PatchedPackResources resources) {
		this.resources = Objects.requireNonNull(resources, "resources");
		this.name = resources.patched$getName();
	}

	public String name() {
		return name;
	}

	public PatchedPackResources resources() {
		return resources;
	}
}