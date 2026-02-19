package net.enderturret.patchedmod.common.internal;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import net.enderturret.patched.IDataSource;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patchedmod.common.SingleDataSource;

/**
 * Patched's implementation of {@link IDataSource}.
 * @author EnderTurret
 */
@Internal
public final class PatchedDataSource implements IDataSource {

	private static final Map<String, SingleDataSource> DATA_SOURCES = new ConcurrentHashMap<>();

	/**
	 * Internal data source registration method.
	 * @param key The name of the data source -- what goes in the {@code type} field.
	 * @param value The data source to register.
	 */
	@Internal
	public static void register(String key, SingleDataSource value) {
		DATA_SOURCES.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
	}

	@Override
	@Nullable
	public JsonElement getData(String type, @Nullable JsonElement from, @Nullable JsonElement value) throws PatchingException {
		final SingleDataSource source = DATA_SOURCES.get(type);
		return source != null ? source.getData(from, value) : null;
	}
}