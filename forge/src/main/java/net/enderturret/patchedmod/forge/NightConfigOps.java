package net.enderturret.patchedmod.forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

/**
 * A kinda half-baked {@link DynamicOps} for NightConfig's... configs.
 * It works mostly fine for decoding, but probably blows up on encode.
 * @author EnderTurret
 */
@Internal
final class NightConfigOps implements DynamicOps<Object> {

	public static final NightConfigOps INSTANCE = new NightConfigOps();

	@Override
	public Object empty() {
		return null;
	}

	@Override
	public <U> U convertTo(DynamicOps<U> outOps, Object input) {
		// Can't be bothered to implement this right now.
		throw new UnsupportedOperationException();
	}

	@Override
	public DataResult<Object> mergeToList(Object list, Object value) {
		if (!(list instanceof List l)) return DataResult.error(() -> "Not a list: " + list);

		final List ret = new ArrayList<>(l);
		ret.add(value);

		return DataResult.success(ret);
	}

	@Override
	public DataResult<Object> mergeToMap(Object map, Object key, Object value) {
		if (!(map instanceof UnmodifiableConfig base)) return DataResult.error(() -> "Map is not a config: " + map);
		if (!(key instanceof String k)) return DataResult.error(() -> "Key is not a string: " + key);

		final CommentedConfig cfg = CommentedConfig.copy(base);
		cfg.add(k, value);

		return DataResult.success(cfg);
	}

	@Override
	public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input) {
		return input instanceof UnmodifiableConfig c ? DataResult.success(
				c.valueMap()
				.entrySet()
				.stream()
				.map(entry -> Pair.of(entry.getKey(), entry.getValue()))) : DataResult.error(() -> "Not an object: " + input);
	}

	@Override
	public Object createMap(Stream<Pair<Object, Object>> map) {
		final Map<String, Object> m = map.map(pair -> {
			if (pair.getFirst() instanceof String)
				return (Pair<String, Object>) (Object) pair;
			else
				throw new IllegalArgumentException("Key must be a string, was: " + pair.getFirst());
		}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

		return CommentedConfig.wrap(m, InMemoryCommentedFormat.defaultInstance());
	}

	@Override
	public DataResult<Stream<Object>> getStream(Object input) {
		return input instanceof List l ? DataResult.success(l.stream()) : DataResult.error(() -> "Not a list: " + input);
	}

	@Override
	public Object createList(Stream<Object> input) {
		return input.collect(Collectors.toList());
	}

	@Override
	public Object remove(Object input, String key) {
		return null;
	}

	@Override
	public DataResult<Number> getNumberValue(Object input) {
		return input instanceof Number n ? DataResult.success(n) : DataResult.error(() -> "Not a number: " + input);
	}

	@Override
	public DataResult<String> getStringValue(Object input) {
		return input instanceof String s ? DataResult.success(s) : DataResult.error(() -> "Not a string: " + input);
	}

	@Override
	public Object createNumeric(Number i) {
		return i;
	}

	@Override
	public Object createString(String value) {
		return value;
	}
}