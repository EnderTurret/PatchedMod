package net.enderturret.patchedmod.util.meta;

import java.util.Locale;

import net.minecraft.server.packs.PackType;
import net.minecraft.util.StringRepresentable;

/**
 * Like {@link PackType}, but serializable.
 * This avoids annoying issues posed by obfuscation at runtime.
 * @author EnderTurret
 */
public enum PatchedPackType implements StringRepresentable {

	/**
	 * See {@link PackType#CLIENT_RESOURCES}.
	 */
	CLIENT_RESOURCES,

	/**
	 * See {@link PackType#SERVER_DATA}.
	 */
	SERVER_DATA;

	/**
	 * Returns the corresponding {@link PackType}.
	 * @return The corresponding {@code PackType}.
	 */
	public PackType toVanilla() {
		return this == CLIENT_RESOURCES ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}