package net.enderturret.patchedmod.common.util.meta;

import java.util.Locale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

/**
 * Like {@code PackType}, but serializable.
 * This avoids annoying issues posed by obfuscation at runtime.
 * @author EnderTurret
 */
public enum PatchedPackType {

	/**
	 * See {@code PackType.CLIENT_RESOURCES}.
	 */
	CLIENT_RESOURCES,

	/**
	 * See {@code PackType.SERVER_DATA}.
	 */
	SERVER_DATA;

	public static final Codec<PatchedPackType> CODEC = Codec.STRING.comapFlatMap(PatchedPackType::forName, PatchedPackType::getSerializedName);

	public final String name = name().toLowerCase(Locale.ENGLISH);

	public static DataResult<PatchedPackType> forName(String name) {
		return switch (name) {
			case "client_resources" -> DataResult.success(CLIENT_RESOURCES);
			case "server_data" -> DataResult.success(SERVER_DATA);
			default -> DataResult.error(() -> "Unknown pack type '" + name + "'");
		};
	}

	public <T> T toVanilla(T client, T server) {
		return this == CLIENT_RESOURCES ? client : server;
	}

	public String getSerializedName() {
		return name;
	}
}