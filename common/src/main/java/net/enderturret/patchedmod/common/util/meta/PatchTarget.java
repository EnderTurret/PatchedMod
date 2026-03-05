package net.enderturret.patchedmod.common.util.meta;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a patch target, as declared in the {@link PatchedMetadata}.
 * @param packType The pack type, if specified.
 * @param patch The patch to apply.
 * @param targets The target files to apply the patch to.
 * @author EnderTurret
 */
public record PatchTarget(Optional<PatchedPackType> packType, String patch, List<Target> targets) {

	public static final Codec<PatchTarget> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			PatchedPackType.CODEC.optionalFieldOf("pack_type").forGetter(PatchTarget::packType),
			Codec.STRING.fieldOf("patch").forGetter(PatchTarget::patch),
			Target.CODEC.listOf().fieldOf("targets").forGetter(PatchTarget::targets)
			).apply(builder, PatchTarget::new));

	/**
	 * Represents an individual file target for a patch target.
	 * @param namespace The list of namespace patterns.
	 * @param path The list of path patterns.
	 * @author EnderTurret
	 */
	public static record Target(List<IPattern> namespace, List<IPattern> path) {

		public static final Codec<Target> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				IPattern.CODEC.listOf().fieldOf("namespace").forGetter(Target::namespace),
				IPattern.CODEC.listOf().fieldOf("path").forGetter(Target::path))
				.apply(builder, Target::new));

		/**
		 * Checks whether the specified namespace matches any of the namespace patterns.
		 * @param namespace The namespace to test.
		 * @return {@code true} if the namespace matches a pattern, or {@code false} otherwise.
		 */
		public boolean matchesNamespace(String namespace) {
			for (IPattern pattern : this.namespace)
				if (pattern.test(namespace))
					return true;

			return false;
		}
	}
}