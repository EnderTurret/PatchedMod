package net.enderturret.patchedmod.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.Patches;
import net.enderturret.patched.patch.JsonPatch;

public final class PatchedFileAccess implements IFileAccess {

	private static final Map<PackResources, Map<String, JsonPatch>> CACHE = new WeakHashMap<>();

	private final PackResources pack;

	public PatchedFileAccess(PackResources pack) {
		this.pack = pack;
	}

	public PackResources pack() {
		return pack;
	}

	@Override
	@Nullable
	public JsonPatch readIncludedPatch(String path) {
		return CACHE.computeIfAbsent(pack, k -> new HashMap<>()).computeIfAbsent(path, k -> {
			try (InputStream is = pack.getRootResource("patches", path).get();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr)) {
				return Patches.readPatch(PatchUtil.GSON, br);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
}