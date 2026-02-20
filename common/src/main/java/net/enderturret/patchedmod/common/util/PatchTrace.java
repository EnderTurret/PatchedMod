package net.enderturret.patchedmod.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.enderturret.patchedmod.common.env.PatchedPackResources;

public abstract sealed class PatchTrace {

	public void recordPatch(PatchedPackResources from, boolean overriden) {}
	public void recordDynamicPatch(PatchedPackResources from, boolean overriden) {}
	public void recordFile(PatchedPackResources from, boolean overriden) {}

	public boolean active() {
		return false;
	}

	public List<String> lines() {
		return List.of();
	}

	public static PatchTrace none() {
		return NoTrace.INSTANCE;
	}

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