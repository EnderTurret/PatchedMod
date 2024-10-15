package net.enderturret.patchedmod.util.meta;

import java.util.function.Function;
import java.util.regex.Pattern;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public sealed interface IPattern {

	public static final Codec<IPattern> CODEC = Codec.either(Simple.CODEC, Regex.CODEC)
			.xmap(either -> either.<IPattern>map(Function.identity(), Function.identity()), pattern -> {
				if (pattern instanceof Simple s)
					return Either.left(s);
				if (pattern instanceof Regex r)
					return Either.right(r);
				return null;
			});

	public boolean test(String value);

	public static record Simple(String target) implements IPattern {

		public static final Codec<Simple> CODEC = Codec.STRING.xmap(Simple::new, Simple::target);

		@Override
		public boolean test(String value) {
			return target.equals(value);
		}

		@Override
		public String toString() {
			return target;
		}
	}

	public static record Regex(Pattern pattern) implements IPattern {

		private static final Codec<Pattern> PATTERN_CODEC = Codec.STRING.xmap(Pattern::compile, Pattern::pattern);

		public static final Codec<Regex> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				PATTERN_CODEC.fieldOf("pattern").forGetter(Regex::pattern)
				).apply(builder, Regex::new));

		@Override
		public boolean test(String value) {
			return pattern.matcher(value).matches();
		}

		@Override
		public String toString() {
			return pattern.pattern();
		}
	}
}