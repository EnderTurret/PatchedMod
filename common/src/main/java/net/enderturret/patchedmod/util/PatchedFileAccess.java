package net.enderturret.patchedmod.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;

import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.Patches;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;

public final class PatchedFileAccess implements IFileAccess {

	private static final LoadingCache<PackResources, Map<String, JsonPatch>> CACHE = CacheBuilder.newBuilder()
			.weakKeys()
			.build(new CacheLoader<PackResources, Map<String, JsonPatch>>() {
				@Override
				public Map<String, JsonPatch> load(PackResources key) throws Exception {
					return new ConcurrentHashMap<>();
				}
			});

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
		try {
			return CACHE.get(pack).computeIfAbsent(path, k -> {
				final IoSupplier<InputStream> sup = pack.getRootResource("patches", path + ".json.patch");
				if (sup == null) throw new PatchingException("Patch patches/" + path + ".json.patch doesn't exist; cannot include it.");

				try (InputStream is = sup.get();
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr)) {
					return Patches.readPatch(PatchUtil.GSON, br);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} catch (Exception e) {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		}
	}
}