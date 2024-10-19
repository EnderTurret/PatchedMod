package net.enderturret.patchedmod.quilt;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.LoaderValue.LArray;
import org.quiltmc.loader.api.LoaderValue.LType;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

/**
 * <p>
 * A {@link DynamicOps} implementation for Quilt's even-less-baked-than-Fabric-custom-values "loader value" API.
 * You cannot construct any of the implementation types. You <i>have</i> to make your own implementations.
 * But even better, everything is {@link NonExtendable}, because why should anyone be able to use these?
 * So the moment you (reluctantly) make your own implementations, Quilt Loader will crash and burn because it
 * upcasted your implementation to some internal package-private interface and immediately died.
 * Everything in Quilt is like this; permanently locked down and unusable.
 * </p>
 * <p>
 * Regardless, this {@code DynamicOps} implementation <i>sucks</i> (even worse than the {@code CustomValue} one), mostly because we can't construct any of these types.
 * It barely works for decoding, and you can bet it'll explode the moment you so much as think about encoding something.
 * </p>
 * <p>
 * And somehow it's <i>still</i> better than writing manual serialization code.
 * </p>
 * @author EnderTurret, very reluctantly
 */
final class LoaderValueOps implements DynamicOps<LoaderValue> {

	public static final LoaderValueOps INSTANCE = new LoaderValueOps();

	@Override
	public String toString() {
		return "LoaderValue";
	}

	@Override
	public LoaderValue empty() {
		// God forbid we provide *constructors* in our clearly-stolen-from-Gson API!
		return new JankNull();
	}

	@Override
	public <U> U convertTo(DynamicOps<U> outOps, LoaderValue input) {
		return switch (input.type()) {
			case ARRAY -> convertList(outOps, input);
			case OBJECT -> convertMap(outOps, input);
			case BOOLEAN -> outOps.createBoolean(input.asBoolean());
			case NULL -> outOps.empty();
			case STRING -> outOps.createString(input.asString());
			// In a serious implementation this has to unpack the value and call the
			// respective method since createNumeric() impls do not do this for us.
			// (Otherwise NBTOps turns them all into doubles. Oops.)
			case NUMBER -> outOps.createNumeric(input.asNumber());
		};
	}

	@Override
	public DataResult<Number> getNumberValue(LoaderValue input) {
		return input.type() == LType.NUMBER ? DataResult.success(input.asNumber()) : DataResult.error(() -> "Not a number: " + input);
	}

	@Override
	public DataResult<Boolean> getBooleanValue(LoaderValue input) {
		return input.type() == LType.BOOLEAN ? DataResult.success(input.asBoolean()) : DataResult.error(() -> "Not a boolean: " + input);
	}

	@Override
	public DataResult<String> getStringValue(LoaderValue input) {
		return input.type() == LType.STRING ? DataResult.success(input.asString()) : DataResult.error(() -> "Not a string: " + input);
	}

	@Override
	public DataResult<Stream<Pair<LoaderValue, LoaderValue>>> getMapValues(LoaderValue input) {
		if (input.type() != LType.OBJECT) return DataResult.error(() -> "Not an object: " + input);
		return DataResult.success(input.asObject().entrySet().stream()
				.map(entry -> Pair.of(new JankString(entry.getKey()), entry.getValue())));
	}

	@Override
	public DataResult<Stream<LoaderValue>> getStream(LoaderValue input) {
		if (input.type() != LType.ARRAY) return DataResult.error(() -> "Not an array: " + input);
		return DataResult.success(StreamSupport.stream(input.asArray().spliterator(), false));
	}

	@Override
	public LoaderValue createString(String value) { return new JankString(value); }

	@Override
	public LoaderValue createNumeric(Number i) { return new JankNumber(i); }

	@Override
	public LoaderValue createList(Stream<LoaderValue> input) { return new JankList(input.toList()); }

	@Override
	public DataResult<LoaderValue> mergeToList(LoaderValue list, LoaderValue value) {
		if (!(list instanceof LArray array)) return DataResult.error(() -> "Not a list: " + list);

		final List<LoaderValue> temp = new ArrayList<>(array.size() + 1);
		for (LoaderValue cv : array) temp.add(cv);
		temp.add(value);

		return DataResult.success(new JankList(List.copyOf(temp)));
	}

	@Override
	public DataResult<LoaderValue> mergeToMap(LoaderValue map, LoaderValue key, LoaderValue value) { throw new UnsupportedOperationException(); }

	@Override
	public LoaderValue createMap(Stream<Pair<LoaderValue, LoaderValue>> map) { throw new UnsupportedOperationException(); }

	@Override
	public LoaderValue remove(LoaderValue input, String key) { throw new UnsupportedOperationException(); }

	// You've heard of /dev/null, you've heard of Dank Null, but wait until you hear about...
	private static final class JankNull implements JankCustomValue {
		@Override
		public LType type() { return LType.NULL; }
	}

	private static record JankString(String value) implements JankCustomValue {
		@Override
		public LType type() { return LType.STRING; }
		@Override
		public String asString() { return value; }
	}

	private static record JankNumber(Number value) implements JankCustomValue {
		@Override
		public LType type() { return LType.NUMBER; }
		@Override
		public Number asNumber() { return value; }
	}

	private static final class JankList extends AbstractList<LoaderValue> implements JankCustomValue, LArray {

		private final List<LoaderValue> list;

		JankList(List<LoaderValue> list) {
			this.list = list;
		}

		@Override
		public LType type() { return LType.ARRAY; }
		@Override
		public Iterator<LoaderValue> iterator() { return list.iterator(); }
		@Override
		public int size() { return list.size(); }
		@Override
		public LoaderValue get(int index) { return list.get(index); }
		@Override
		public LArray asArray() { return this; }
		@Override
		public boolean equals(Object obj) { return obj instanceof JankList jl && list.equals(jl.list); }
		@Override
		public int hashCode() { return list.hashCode(); }

		@Override
		public Stream<LoaderValue> stream() { return list.stream(); }
	}

	private static interface JankCustomValue extends LoaderValue {
		@Override
		public default LObject asObject() { throw new UnsupportedOperationException(); }
		@Override
		public default LArray asArray() { throw new UnsupportedOperationException(); }
		@Override
		public default String asString() { throw new UnsupportedOperationException(); }
		@Override
		public default Number asNumber() { throw new UnsupportedOperationException(); }
		@Override
		public default boolean asBoolean() { throw new UnsupportedOperationException(); }
		@Override
		public default String location() { return "location not supported"; }
	}
}