package net.enderturret.patchedmod.common.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.flow.DynamicPatches;
import net.enderturret.patchedmod.common.internal.flow.PatchingManager;
import net.enderturret.patchedmod.common.util.meta.IPattern;
import net.enderturret.patchedmod.common.util.meta.PatchTarget;
import net.enderturret.patchedmod.common.util.meta.PatchTarget.Target;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * {@code PatchTargetManager}, as the name may suggest, manages patch targets.
 * These are defined in individual packs' {@link PatchedMetadata} and allow patching many files based on one or more regular expressions.
 * Reading all of this data every time a file is patched isn't quick however, so this class tracks this information to speed things up.
 * @author EnderTurret
 */
@Internal
public final class PatchTargetManager {

	private static final boolean USES_UNPREFIXED_IDS = PatchedPlatform.get().hasUnprefixedPackIds();

	private final PatchedPackType type;
	@Nullable
	private final List<PatchedPackResources> packsByPriority; // Organized by priority, exactly like the resource pack screen.
	@Nullable
	private final Map<String, Integer> priorityByPack;

	private final List<BakedTarget> targets;
	@Nullable
	private final Map<String, List<BakedTarget>> targetsByNamespace;

	/**
	 * Constructs a new {@code PatchTargetManager} with the specified pack type and list of packs.
	 * @param type The pack type.
	 * @param packsByPriority The list of packs, ordered by priority.
	 */
	@Internal
	public PatchTargetManager(PatchedPackType type, List<PatchedPackResources> packsByPriority) {
		this.type = type;

		if (PatchedPlatform.get().hasGroupPacks())
			packsByPriority = packsByPriority.stream()
					.flatMap(pack -> (pack.patched$isGroupPack() ? pack.patched$getChildren() : List.of(pack)).stream())
					.toList();
		else
			packsByPriority = List.copyOf(packsByPriority);

		final Map<String, Integer> priorityByPack = new IdentityHashMap<>();
		final List<BakedTarget> targets = new ArrayList<>();

		for (int i = 0; i < packsByPriority.size(); i++) {
			final PatchedPackResources pack = packsByPriority.get(i);

			priorityByPack.put(pack.patched$packId().intern(), i);

			for (PatchTarget target : pack.patchedMetadata().patchTargets())
				if (target.packType().orElse(type) == type)
					for (Target subTarget : target.targets())
						targets.add(new BakedTarget(subTarget, target.patch(), pack));
		}

		this.targets = List.copyOf(targets);
		final boolean empty = this.targets.isEmpty();
		targetsByNamespace = empty ? null : new HashMap<>();
		this.packsByPriority = empty ? null : List.copyOf(packsByPriority);
		this.priorityByPack = priorityByPack;

		if (DynamicPatches.DEBUG_TARGETS || PatchingManager.DEBUG)
			PatchedInternal.LOGGER.info("Built PatchTargetManager {} with {}", type.name(), packsByPriority.stream()
					.map(pr -> pr.toString() + " (" + pr.patched$packId() + ")").collect(Collectors.joining(", ")));

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

	/**
	 * From the specified location and pack, builds up a list of applicable patches to apply to the file and returns it.
	 * @param loc The location of the file being patched.
	 * @param from The pack the file originated from. Determines which other packs to take into account.
	 * @return The list of all applicable patches, paired with their owning packs (for priority handling).
	 */
	@Internal
	public Map<PatchedPackResources, List<String>> getTargets(PatchedResourceLocation loc, PatchedPackResources from) {
		if (targetsByNamespace == null) return Map.of();

		bakeNamespace(loc.patched$getNamespace());

		final List<BakedTarget> targets = targetsByNamespace.get(loc.patched$getNamespace());
		if (targets.isEmpty()) return Map.of();

		final int fromIndex = Objects.requireNonNull(priorityByPack.get(from.patched$packId().intern()),
				"Priority for pack " + from + " (" + from.patched$packId() + ") doesn't exist, was the pack registered?");

		final Map<PatchedPackResources, List<String>> ret = new IdentityHashMap<>(targets.size());

		// Cache the last list used in the loop so we don't need to perform 40 lookups.
		PatchedPackResources lastPack = null;
		int lastIdx = -1;
		List<String> lastList = null;

		parent:
		for (BakedTarget target : targets) {
			if (lastPack != target.from) {
				lastPack = target.from;
				lastIdx = priorityByPack.get(lastPack.patched$packId().intern());
				lastList = null; // Here we avoid creating hundreds of ArrayLists in the event there's no relevant targets.
			}

			if (DynamicPatches.DEBUG_TARGETS)
				PatchedInternal.LOGGER.info("Processing {} with last values {}, {}, {}...", target, lastPack, lastIdx, lastList);

			// Don't allow patches from lower packs to affect a replacement from a higher one.
			final int idx = priorityByPack.get(target.from.patched$packId().intern());

			if (DynamicPatches.DEBUG_TARGETS)
				PatchedInternal.LOGGER.info("  Priority check: {} < {}?", idx, fromIndex);

			if (idx < fromIndex) break;

			if (DynamicPatches.DEBUG_TARGETS)
				PatchedInternal.LOGGER.info("  Trying patterns {} on {}", target.target().path(), loc.patched$getPath());

			for (IPattern pattern : target.target().path()) {
				if (DynamicPatches.DEBUG_TARGETS)
					PatchedInternal.LOGGER.info("    Trying pattern {} ({}) on {}", pattern, pattern.getClass().getSimpleName(), loc.patched$getPath());

				if (pattern.test(loc.patched$getPath())) {
					if (lastList == null)
						lastList = ret.computeIfAbsent(lastPack, k -> new ArrayList<>(5));

					lastList.add(target.patch);

					if (DynamicPatches.DEBUG_TARGETS)
						PatchedInternal.LOGGER.info("    Success: added {} to {}", target.patch, lastList);

					continue parent;
				}
			}
		}

		if (DynamicPatches.DEBUG_TARGETS)
			PatchedInternal.LOGGER.info("Returning {}", ret);

		return ret;
	}

	/**
	 * Returns whether or not the specified pack ID is tracked by this {@code PatchTargetManager}.
	 * @param name The pack ID to test.
	 * @return {@code true} if the specified pack is tracked.
	 */
	@Internal
	public boolean containsPack(String name) {
		if (USES_UNPREFIXED_IDS)
			if (name.startsWith("file/"))
				name = name.substring("file/".length());
			else if ("vanilla".equals(name))
				name = "Default";

		return priorityByPack.containsKey(name.intern());
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

	static record BakedTarget(Target target, String patch, PatchedPackResources from) {}
}