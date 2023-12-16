package net.enderturret.patchedmod.forge;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.VanillaPackResources;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforgespi.language.IModInfo;

import net.enderturret.patchedmod.mixin.forge.DelegatingPackResourcesAccess;
import net.enderturret.patchedmod.util.env.IPlatform;

final class ForgePlatform implements IPlatform {

	private final Logger logger = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return logger;
	}

	@Override
	public boolean isPhysicalClient() {
		return FMLEnvironment.dist == Dist.CLIENT;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isModLoaded(String modId, String version) {
		return ModList.get().getModContainerById(modId)
				.map(mc -> {
					final ArtifactVersion theirVersion = mc.getModInfo().getVersion();
					final DefaultArtifactVersion realVersion = new DefaultArtifactVersion(version);
					return theirVersion.compareTo(realVersion);
				})
				.orElse(-1) >= 0;
	}

	@Override
	public PackOutput getPackOutput(DataGenerator generator) {
		return generator.getPackOutput();
	}

	@Override
	public String getName(PackResources pack) {
		if (pack instanceof PathPackResources) {
			final Optional<String> mod = findModNameFromModFile(pack.packId());
			if (mod.isPresent())
				return "mod/" + mod.get();
		}

		return pack.packId();
	}

	private static Optional<String> findModNameFromModFile(String modFile) {
		return ModList.get().getModFiles()
				.stream()
				.filter(mfi -> modFile.equals(mfi.getFile().getFileName()))
				.flatMap(mfi -> mfi.getMods().stream())
				.map(IModInfo::getDisplayName)
				.findFirst();
	}

	@Override
	public boolean isGroup(PackResources pack) {
		return pack instanceof DelegatingPackResourcesAccess;
	}

	@Override
	public Collection<PackResources> getChildren(PackResources pack) {
		return Objects.requireNonNullElse(pack.getChildren(), List.of());
	}

	@Override
	public Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, ResourceLocation file) {
		return pack instanceof DelegatingPackResourcesAccess dpra ? dpra.callGetCandidatePacks(type, file) : List.of();
	}

	@Override
	public boolean needsSwapNamespaceAndPath(PackResources pack) {
		return true;
	}

	@Override
	public Function<ResourceLocation, ResourceLocation> getRenamer(PackResources pack, String namespace) {
		final boolean vanilla = pack instanceof VanillaPackResources;
		final int prefixLen = vanilla ? "../".length() : 0;
		// PathPackResources:     :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:  :../minecraft/something → minecraft:something
		return rl -> new ResourceLocation(namespace, rl.getPath().substring(prefixLen + namespace.length() + 1));
	}
}