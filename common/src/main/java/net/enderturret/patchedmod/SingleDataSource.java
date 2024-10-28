package net.enderturret.patchedmod;

import java.util.function.BinaryOperator;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;

import net.enderturret.patched.IDataSource;
import net.enderturret.patched.exception.PatchingException;

/**
 * Represents a typeless {@link IDataSource} for use with {@code paste} patches.
 * Mods can register their own using {@link Patched#registerDataSource(ResourceLocation, SingleDataSource)}.
 * @author EnderTurret
 */
public interface SingleDataSource {

	/**
	 * Retrieves the data associated with this data source, optionally using the patch-provided {@code value} argument.
	 * @param from The element pointed to by the {@code from} path in the patch. May be {@code null} if the patch did not specify one.
	 * @param value The {@code value} field specified in the patch. May be {@code null} if the patch did not specify one.
	 * @return The corresponding data.
	 * @throws PatchingException If an error occurs retrieving the data (such as missing or invalid context).
	 */
	public JsonElement getData(@Nullable JsonElement from, @Nullable JsonElement value) throws PatchingException;

	/**
	 * Wraps the given Java {@link BinaryOperator} into a {@code SingleDataSource}.
	 * @param source The data source to wrap.
	 * @return The wrapped data source.
	 */
	public static SingleDataSource wrap(BinaryOperator<JsonElement> source) {
		return (from, value) -> source.apply(from, value);
	}
}