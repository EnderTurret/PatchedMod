package net.enderturret.patchedmod.util.meta;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.StringRepresentable;

public record PatchTarget(Optional<PatchedPackType> packType, String patch, List<Target> targets) {

	private static final Codec<PatchedPackType> PACK_TYPE_CODEC = StringRepresentable.fromEnum(PatchedPackType::values);

	public static final Codec<PatchTarget> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			PACK_TYPE_CODEC.optionalFieldOf("pack_type").forGetter(PatchTarget::packType),
			Codec.STRING.fieldOf("patch").forGetter(PatchTarget::patch),
			Target.CODEC.listOf().fieldOf("targets").forGetter(PatchTarget::targets)
			).apply(builder, PatchTarget::new));

	public static record Target(List<IPattern> namespace, List<IPattern> path) {

		public static final Codec<Target> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				IPattern.CODEC.listOf().fieldOf("namespace").forGetter(Target::namespace),
				IPattern.CODEC.listOf().fieldOf("path").forGetter(Target::path))
				.apply(builder, Target::new));
	}
}