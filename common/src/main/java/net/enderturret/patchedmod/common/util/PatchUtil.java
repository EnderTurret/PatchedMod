package net.enderturret.patchedmod.common.util;

/**
 * An assortment of utilities related to patching Json data.
 * @author EnderTurret
 */
public final class PatchUtil {

	/**
	 * @param path The location of the file to test.
	 * @return {@code true} if the file at the given location supports being patched, based on its name.
	 */
	public static boolean isPatchable(String path) {
		return path.endsWith(".json") || (path.endsWith(".mcmeta") && !path.equals("pack.mcmeta"));
	}
}