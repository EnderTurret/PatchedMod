package net.enderturret.patchedmod.mixin.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.loader.api.metadata.ModMetadata;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;

import net.enderturret.patchedmod.common.env.IPatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;
import net.enderturret.patchedmod.fabric.FabricPlatform;
import net.enderturret.patchedmod.fabric.IFabricModPackResources;

@Mixin(PackResources.class)
public interface MixinPackResources extends IPatchingPackResources {

	@Override
	public default String patched$packId() {
		return ((PackResources) this).packId();
	}

	@Override
	public default boolean patched$isVanillaPack() {
		return this instanceof VanillaPackResources;
	}

	@Override
	public default Set<String> patched$getNamespaces(PatchedPackType type) {
		return ((PackResources) this).getNamespaces(type.toVanilla(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA));
	}

	@Override
	public default @Nullable InputStream patched$getRootResource(String... path) throws IOException {
		final IoSupplier<InputStream> ret = ((PackResources) this).getRootResource(path);
		return ret != null ? ret.get() : null;
	}

	@Override
	public default String patched$getName() {
		final ModMetadata mod = FabricPlatform.getModMetadataFromPack(this);
		if (mod != null) {
			final String modId = mod.getId();
			final String packId;

			if (!modId.equals(patched$packId()))
				if (patched$packId().startsWith(modId)) {
					final String temp = patched$packId().substring(modId.length());
					packId = temp.startsWith(":") ? temp.substring(1) : temp;
				} else
					packId = patched$packId();
			else
				packId = null;

			return "mod/" + mod.getName() + (packId != null ? "/" + packId : "");
		}

		return patched$packId();
	}

	@Override
	public default boolean patched$needsSwapNamespaceAndPath() {
		// Fabric implementations surprisingly throw no errors, unlike Minecraft.
		return !(this instanceof IFabricModPackResources);
	}

	@Override
	public default Function<Identifier, Identifier> patched$getRenamer(String namespace) {
		// GroupResourcePack and ModNioResourcePack
		if (!patched$needsSwapNamespaceAndPath()) return Function.identity();
		// PathPackResources:      :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:  .:minecraft/something → minecraft:something
		return rl -> Identifier.fromNamespaceAndPath(namespace, rl.getPath().substring(namespace.length() + 1));
	}
}