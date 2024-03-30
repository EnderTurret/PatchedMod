package net.enderturret.patchedmod.util.meta;

import java.util.Locale;

import net.minecraft.server.packs.PackType;
import net.minecraft.util.StringRepresentable;

public enum PatchedPackType implements StringRepresentable {

	CLIENT_RESOURCES,
	SERVER_DATA;

	public PackType toVanilla() {
		return this == CLIENT_RESOURCES ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}