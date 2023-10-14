package net.enderturret.patchedmod.forge;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.resource.PathResourcePack;

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
	public String getName(PackResources pack) {
		return pack instanceof PathResourcePack ppp ? "mod/" + findModNameFromModFile(pack.getName()) : pack.getName();
	}

	private static String findModNameFromModFile(String modFile) {
		return ModList.get().getModFiles()
				.stream()
				.filter(mfi -> modFile.equals(mfi.getFile().getFileName()))
				.flatMap(mfi -> mfi.getMods().stream())
				.map(IModInfo::getDisplayName)
				.findFirst().orElse(modFile);
	}

	@Override
	public boolean isGroup(PackResources pack) {
		return pack instanceof DelegatingPackResourcesAccess;
	}

	@Override
	public Collection<PackResources> getChildren(PackResources pack) {
		return pack instanceof DelegatingPackResourcesAccess dpra ? dpra.getDelegates() : List.of();
	}

	@Override
	public Collection<PackResources> getFilteredChildren(PackResources pack, PackType type, ResourceLocation file) {
		return pack instanceof DelegatingPackResourcesAccess dpra ? dpra.callGetCandidatePacks(type, file) : List.of();
	}

	@Override
	public Function<ResourceLocation, ResourceLocation> getRenamer(PackResources pack, String namespace) {
		// DelegatingResourcePack & PathResourcePack
		if (isGroup(pack) || pack instanceof PathResourcePack || pack instanceof VanillaPackResources)
			return rl -> rl;
		// PathPackResources:     minecraft:/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:  minecraft:/something → minecraft:something
		return rl -> new ResourceLocation(namespace, rl.getPath().substring(1));
	}
}