package net.enderturret.patchedmod.forge;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.resource.PathPackResources;

import net.enderturret.patchedmod.mixin.forge.DelegatingPackResourcesAccess;
import net.enderturret.patchedmod.util.env.IPlatform;

public final class ForgePlatform implements IPlatform {

	private static final Logger LOGGER = LoggerFactory.getLogger("Patched");

	@Override
	public Logger logger() {
		return LOGGER;
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
	public String getName(PackResources pack) {
		return pack instanceof PathPackResources ppp ? "mod/" + findModNameFromModFile(pack.getName()) : pack.getName();
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
}