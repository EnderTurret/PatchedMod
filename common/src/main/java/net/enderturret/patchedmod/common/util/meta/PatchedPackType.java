package net.enderturret.patchedmod.common.util.meta;

import java.util.Locale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.enderturret.patchedmod.common.internal.env.PatchedPlatform;

/**
 * Like {@code PackType}, but serializable.
 * This avoids annoying issues posed by obfuscation at runtime.
 * @author EnderTurret
 */
public enum PatchedPackType {

	/**
	 * See {@code PackType.CLIENT_RESOURCES}.
	 */
	CLIENT_RESOURCES("assets"),

	/**
	 * See {@code PackType.SERVER_DATA}.
	 */
	SERVER_DATA("data");

	/**
	 * A {@link Codec} for {@code PatchedPackType}.
	 */
	public static final Codec<PatchedPackType> CODEC = Codec.STRING.comapFlatMap(PatchedPackType::forName, PatchedPackType::getSerializedName);

	/**
	 * The name of the pack type.
	 * This is like the "serialized name" of a {@code StringRepresentable}.
	 */
	public final String name = name().toLowerCase(Locale.ENGLISH);

	/**
	 * The name of the directory this pack type represents.
	 * This will either be "assets" or "data".
	 */
	public final String directory;

	private PatchedPackType(String directory) {
		this.directory = directory;
	}

	/**
	 * Returns a {@link DataResult} representing the pack type corresponding to the specified name.
	 * @param name The name of the desired pack type.
	 * @return A {@code DataResult} containing the pack type, or an error result.
	 */
	public static DataResult<PatchedPackType> forName(String name) {
		return switch (name) {
			case "client_resources" -> PatchedPlatform.get().success(CLIENT_RESOURCES);
			case "server_data" -> PatchedPlatform.get().success(SERVER_DATA);
			default -> PatchedPlatform.get().error(() -> "Unknown pack type '" + name + "'");
		};
	}

	/**
	 * Converts this pack type into one of the specified vanilla pack types.
	 * @param <T> The type of the vanilla pack type.
	 * @param client The pack type instance for the client.
	 * @param server The pack type instance for the server.
	 * @return The corresponding vanilla pack type.
	 */
	public <T> T toVanilla(T client, T server) {
		return this == CLIENT_RESOURCES ? client : server;
	}

	/**
	 * Returns the name of the pack type.
	 * @return {@link #name}.
	 */
	public String getSerializedName() {
		return name;
	}
}