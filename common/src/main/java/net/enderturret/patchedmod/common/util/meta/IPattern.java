package net.enderturret.patchedmod.common.util.meta;

import java.util.function.Function;
import java.util.regex.Pattern;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a type of pattern, which may be either a regular expression or a literal string.
 * @author EnderTurret
 */
public sealed interface IPattern {

	/**
	 * The pattern codec.
	 */
	public static final Codec<IPattern> CODEC = Codec.either(Simple.CODEC, Regex.CODEC)
			.xmap(either -> either.<IPattern>map(Function.identity(), Function.identity()), pattern -> {
				if (pattern instanceof Simple s)
					return Either.left(s);
				if (pattern instanceof Regex r)
					return Either.right(r);
				return null;
			});

	/**
	 * Attempts to match the specified string against the pattern, returning {@code true} if a match was found.
	 * @param value The input string.
	 * @return {@code true} if the input string matches the pattern, {@code false} otherwise.
	 */
	public boolean test(String value);

	/**
	 * The string literal implementation of {@code IPattern}.
	 * @param target The literal string to test against.
	 * @author EnderTurret
	 */
	public static record Simple(String target) implements IPattern {

		/**
		 * The codec for simple patterns.
		 */
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

	/**
	 * The regular expression implementation of {@code IPattern}.
	 * @param pattern The regular expression to test against.
	 * @author EnderTurret
	 */
	public static record Regex(Pattern pattern) implements IPattern {

		private static final Codec<Pattern> PATTERN_CODEC = Codec.STRING.xmap(Pattern::compile, Pattern::pattern);

		/**
		 * The codec for regular expression patterns.
		 */
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