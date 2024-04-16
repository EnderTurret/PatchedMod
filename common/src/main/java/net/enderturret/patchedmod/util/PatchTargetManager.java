package net.enderturret.patchedmod.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.util.MixinCallbacks.Entry;
import net.enderturret.patchedmod.util.meta.IPattern;
import net.enderturret.patchedmod.util.meta.PatchTarget;
import net.enderturret.patchedmod.util.meta.PatchTarget.Target;
import net.enderturret.patchedmod.util.meta.PatchedPackType;

@Internal
public final class PatchTargetManager {

	private final PackType type;
	private final List<PackResources> packsByPriority; // Organized by priority, exactly like the resource pack screen.
	private final Map<PackResources, Integer> priorityByPack;

	private final List<BakedTarget> targets;
	@Nullable
	private final Map<String, List<BakedTarget>> targetsByNamespace;

	PatchTargetManager(PackType type, List<PackResources> packsByPriority) {
		this.type = type;

		final Map<PackResources, Integer> priorityByPack = new IdentityHashMap<>();
		final List<BakedTarget> targets = new ArrayList<>();

		for (int i = 0; i < packsByPriority.size(); i++) {
			final PackResources pack = packsByPriority.get(i);

			priorityByPack.put(pack, i);

			if (pack instanceof IPatchingPackResources ppp)
				for (PatchTarget target : ppp.patchedMetadata().patchTargets())
					if (target.packType().map(PatchedPackType::toVanilla).orElse(type) == type)
						for (Target subTarget : target.targets())
							targets.add(new BakedTarget(subTarget, target.patch(), pack));
		}

		this.targets = List.copyOf(targets);
		final boolean empty = this.targets.isEmpty();
		targetsByNamespace = empty ? null : new HashMap<>();
		this.packsByPriority = empty ? null : List.copyOf(packsByPriority);
		this.priorityByPack = empty ? null : priorityByPack;

		bakeNamespace("minecraft"); // This is the single-most likely filled namespace.
	}

	private void bakeNamespace(String ns) {
		if (targetsByNamespace == null) return;

		final List<BakedTarget> targets = new ArrayList<>();

		parent:
		for (BakedTarget target : this.targets)
			for (IPattern pattern : target.target.namespace())
				if (pattern.test(ns)) {
					targets.add(target);
					continue parent;
				}

		targetsByNamespace.put(ns, List.copyOf(targets));
	}

	Map<PackResources, List<String>> getTargets(ResourceLocation loc, PackResources from) {
		if (targetsByNamespace == null) return Map.of();

		bakeNamespace(loc.getNamespace());

		final int fromIndex = priorityByPack.get(from);

		final List<BakedTarget> targets = targetsByNamespace.get(loc.getNamespace());
		final Map<PackResources, List<String>> ret = new IdentityHashMap<>(targets.size());

		// Cache the last list used in the loop so we don't need to perform 40 lookups.
		PackResources lastPack = null;
		int lastIdx = -1;
		List<String> lastList = null;

		parent:
		for (BakedTarget target : targets) {
			if (lastPack != target.from) {
				lastPack = target.from;
				lastIdx = priorityByPack.get(lastPack);
				lastList = null; // Here we avoid creating hundreds of ArrayLists in the event there's no relevant targets.
			}

			if (MixinCallbacks.DEBUG_TARGETS)
				Patched.platform().logger().info("Processing {} with last values {}, {}, {}...", target, lastPack, lastIdx, lastList);

			// Don't allow patches from lower packs to affect a replacement from a higher one.
			final int idx = priorityByPack.get(target.from);

			if (MixinCallbacks.DEBUG_TARGETS)
				Patched.platform().logger().info("  Priority check: {} < {}?", idx, fromIndex);

			if (idx < fromIndex) break;

			if (MixinCallbacks.DEBUG_TARGETS)
				Patched.platform().logger().info("  Trying patterns {} on {}", target.target().path(), loc.getPath());

			for (IPattern pattern : target.target().path()) {
				if (MixinCallbacks.DEBUG_TARGETS)
					Patched.platform().logger().info("    Trying pattern {} ({}) on {}", pattern, pattern.getClass().getSimpleName(), loc.getPath());

				if (pattern.test(loc.getPath())) {
					if (lastList == null)
						lastList = ret.computeIfAbsent(lastPack, k -> new ArrayList<>(5));

					lastList.add(target.patch);

					if (MixinCallbacks.DEBUG_TARGETS)
						Patched.platform().logger().info("    Success: added {} to {}", target.patch, lastList);

					continue parent;
				}
			}
		}

		if (MixinCallbacks.DEBUG_TARGETS)
			Patched.platform().logger().info("Returning {}", ret);

		return ret;
	}

	@Override
	public String toString() {
		return ("PatchTargetManager {"
				+ "\n    type = %s,"
				+ "\n    packsByPriority = %s,"
				+ "\n    priorityByPack = %s,"
				+ "\n    targets = %s,"
				+ "\n    targetsByNamespace = %s"
				+ "\n}").formatted(type, packsByPriority, priorityByPack, targets, targetsByNamespace);
	}

	static record BakedTarget(Target target, String patch, PackResources from) {}
}