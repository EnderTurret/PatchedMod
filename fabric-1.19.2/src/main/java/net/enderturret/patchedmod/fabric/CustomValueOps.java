package net.enderturret.patchedmod.fabric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvArray;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;

/**
 * <p>
 * A {@link DynamicOps} implementation for Fabric's beyond-half-baked "custom value" API.
 * You cannot construct any of the implementation types. You <i>have</i> to make your own implementations.
 * Everything in Fabric API is like this.
 * </p>
 * <p>
 * Regardless, this {@code DynamicOps} implementation <i>sucks</i>, mostly because we can't construct any of these types.
 * It barely works for decoding, and you can bet it'll explode the moment you so much as think about encoding something.
 * </p>
 * <p>
 * And somehow it's <i>still</i> better than writing manual serialization code.
 * </p>
 * @author EnderTurret, reluctantly
 */
final class CustomValueOps implements DynamicOps<CustomValue> {

	public static final CustomValueOps INSTANCE = new CustomValueOps();

	@Override
	public String toString() {
		return "CustomValue";
	}

	@Override
	public CustomValue empty() {
		// God forbid we provide *constructors* in our clearly-stolen-from-Gson API!
		return new JankNull();
	}

	@Override
	public <U> U convertTo(DynamicOps<U> outOps, CustomValue input) {
		return switch (input.getType()) {
			case ARRAY -> convertList(outOps, input);
			case OBJECT -> convertMap(outOps, input);
			case BOOLEAN -> outOps.createBoolean(input.getAsBoolean());
			case NULL -> outOps.empty();
			case STRING -> outOps.createString(input.getAsString());
			// In a serious implementation this has to unpack the value and call the
			// respective method since createNumeric() impls do not do this for us.
			// (Otherwise NBTOps turns them all into doubles. Oops.)
			case NUMBER -> outOps.createNumeric(input.getAsNumber());
		};
	}

	@Override
	public DataResult<Number> getNumberValue(CustomValue input) {
		return input.getType() == CvType.NUMBER ? DataResult.success(input.getAsNumber()) : DataResult.error(() -> "Not a number: " + input);
	}

	@Override
	public DataResult<Boolean> getBooleanValue(CustomValue input) {
		return input.getType() == CvType.BOOLEAN ? DataResult.success(input.getAsBoolean()) : DataResult.error(() -> "Not a boolean: " + input);
	}

	@Override
	public DataResult<String> getStringValue(CustomValue input) {
		return input.getType() == CvType.STRING ? DataResult.success(input.getAsString()) : DataResult.error(() -> "Not a string: " + input);
	}

	@Override
	public DataResult<Stream<Pair<CustomValue, CustomValue>>> getMapValues(CustomValue input) {
		if (input.getType() != CvType.OBJECT) return DataResult.error(() -> "Not an object: " + input);
		return DataResult.success(StreamSupport.stream(input.getAsObject().spliterator(), false)
				.map(entry -> Pair.of(new JankString(entry.getKey()), entry.getValue())));
	}

	@Override
	public DataResult<Stream<CustomValue>> getStream(CustomValue input) {
		if (input.getType() != CvType.ARRAY) return DataResult.error(() -> "Not an array: " + input);
		return DataResult.success(StreamSupport.stream(input.getAsArray().spliterator(), false));
	}

	@Override
	public CustomValue createString(String value) { return new JankString(value); }

	@Override
	public CustomValue createNumeric(Number i) { return new JankNumber(i); }

	@Override
	public CustomValue createList(Stream<CustomValue> input) { return new JankList(input.toList()); }

	@Override
	public DataResult<CustomValue> mergeToList(CustomValue list, CustomValue value) {
		if (!(list instanceof CvArray array)) return DataResult.error(() -> "Not a list: " + list);

		final List<CustomValue> temp = new ArrayList<>(array.size() + 1);
		for (CustomValue cv : array) temp.add(cv);
		temp.add(value);

		return DataResult.success(new JankList(List.copyOf(temp)));
	}

	@Override
	public DataResult<CustomValue> mergeToMap(CustomValue map, CustomValue key, CustomValue value) { throw new UnsupportedOperationException(); }

	@Override
	public CustomValue createMap(Stream<Pair<CustomValue, CustomValue>> map) { throw new UnsupportedOperationException(); }

	@Override
	public CustomValue remove(CustomValue input, String key) { throw new UnsupportedOperationException(); }

	// You've heard of /dev/null, you've heard of Dank Null, but wait until you here about...
	private static final class JankNull implements JankCustomValue {
		@Override
		public CvType getType() { return CvType.NULL; }
	}

	private static record JankString(String value) implements JankCustomValue {
		@Override
		public CvType getType() { return CvType.STRING; }
		@Override
		public String getAsString() { return value; }
	}

	private static record JankNumber(Number value) implements JankCustomValue {
		@Override
		public CvType getType() { return CvType.NUMBER; }
		@Override
		public Number getAsNumber() { return value; }
	}

	private static record JankList(List<CustomValue> list) implements JankCustomValue, CvArray {
		@Override
		public CvType getType() { return CvType.ARRAY; }
		@Override
		public Iterator<CustomValue> iterator() { return list.iterator(); }
		@Override
		public int size() { return list.size(); }
		@Override
		public CustomValue get(int index) { return list.get(index); }
		@Override
		public CvArray getAsArray() { return this; }
	}

	private static interface JankCustomValue extends CustomValue {
		@Override
		public default CvObject getAsObject() { throw new UnsupportedOperationException(); }
		@Override
		public default CvArray getAsArray() { throw new UnsupportedOperationException(); }
		@Override
		public default String getAsString() { throw new UnsupportedOperationException(); }
		@Override
		public default Number getAsNumber() { throw new UnsupportedOperationException(); }
		@Override
		public default boolean getAsBoolean() { throw new UnsupportedOperationException(); }
	}
}