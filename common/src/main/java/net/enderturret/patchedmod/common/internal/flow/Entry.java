package net.enderturret.patchedmod.common.internal.flow;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;

/**
 * An alternative to transforming {@code PackEntry}'s constructor public.
 * @author EnderTurret
 */
@Internal
final class Entry {

	private final PatchedPackResources resources;
	private @Nullable String name;

	/**
	 * Constructs a new {@code Entry}.
	 * @param resources The pack itself.
	 */
	Entry(PatchedPackResources resources) {
		this.resources = Objects.requireNonNull(resources, "resources");
	}

	public String name() {
		if (name == null)
			name = Objects.requireNonNull(resources.patched$getName(), "name");

		return name;
	}

	public PatchedPackResources resources() {
		return resources;
	}
}