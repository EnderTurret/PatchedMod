package net.enderturret.patchedmod.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;

/**
 * Represents the contents of an invocation of the {@code /patched trace} subcommand.
 * It can be passed to a {@link PatchingInputStream} to fill out information about the packs whose patches were applied.
 * @author EnderTurret
 * @see PatchingInputStream
 */
public abstract sealed class PatchTrace {

	/**
	 * Records that a patch was applied (or overriden) from the specified pack.
	 * @param from The pack whose patch was applied or overriden.
	 * @param overriden Whether or not the patch was overriden instead of being applied.
	 */
	public void recordPatch(PatchedPackResources from, boolean overriden) {}

	/**
	 * Records that a dynamic patch was applied (or overriden) from the specified pack.
	 * @param from The pack whose dynamic patch was applied or overriden.
	 * @param overriden Whether or not the dynamic patch was overriden instead of being applied.
	 */
	public void recordDynamicPatch(PatchedPackResources from, boolean overriden) {}

	/**
	 * Records that a file was sourced (or overriden) from the specified pack.
	 * These always indicate the bottom of the applied part of a trace, since a traced file has to come from somewhere.
	 * @param from The pack whose file was read or overriden.
	 * @param overriden Whether or not the file was overriden by some other file.
	 */
	public void recordFile(PatchedPackResources from, boolean overriden) {}

	/**
	 * Returns whether or not this {@code PatchTrace} records any data.
	 * This is used to determine whether to collect information about overriden files and patches — something which is relatively expensive.
	 * @return {@code true} if the {@code PatchTrace} should be filled out, or {@code false} otherwise.
	 */
	public boolean active() {
		return false;
	}

	/**
	 * Returns a copy of the list of lines recorded by this {@code PatchTrace}.
	 * The lines will be in encounter order, with the least priority pack at the beginning of the list.
	 * (This is reverse order compared to the resource pack screen or the output of {@code /patched trace}.)
	 * @return The list of recorded lines.
	 */
	public List<String> lines() {
		return List.of();
	}

	/**
	 * Returns a {@code PatchTrace} that does not record any data.
	 * @return An empty, no-op {@code PatchTrace}.
	 */
	public static PatchTrace none() {
		return NoTrace.INSTANCE;
	}

	/**
	 * Returns a new {@code PatchTrace} ready for recording data.
	 * @return A new {@code PatchTrace}.
	 * @see PatchingInputStream#withTrace(PatchTrace)
	 */
	public static PatchTrace newInstance() {
		return new Impl();
	}

	private static final class NoTrace extends PatchTrace {
		private static final NoTrace INSTANCE = new NoTrace();
	}

	private static final class Impl extends PatchTrace {

		private final List<String> lines = new ArrayList<>();

		@Override
		public void recordPatch(PatchedPackResources from, boolean overriden) {
			lines.add((overriden ? "# patch from %s is overriden" : "├ patch from %s applies").formatted(from.patched$getName()));
		}

		@Override
		public void recordDynamicPatch(PatchedPackResources from, boolean overriden) {
			lines.add((overriden ? "# dynamic patch from %s is overriden" : "├ dynamic patch from %s applies").formatted(from.patched$getName()));
		}

		@Override
		public void recordFile(PatchedPackResources from, boolean overriden) {
			lines.add((overriden ? "# file from %s is overriden" : "└ file from %s applies").formatted(from.patched$getName()));
		}

		@Override
		public boolean active() {
			return true;
		}

		@Override
		public List<String> lines() {
			final List<String> ret = new ArrayList<>(lines);
			Collections.reverse(ret);
			return List.copyOf(ret);
		}
	}
}