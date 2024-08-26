package net.enderturret.patchedmod.forge;

import java.util.Optional;
import java.util.function.Function;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.VanillaPackResources;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

import net.enderturret.patchedmod.util.env.IPlatform;
import net.enderturret.patchedmod.util.meta.PatchedMetadata;

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
		final Optional<? extends ModContainer> mod = findModNameFromModFile(pack);

		if (mod.isPresent())
			return "mod/" + mod.get().getModInfo().getDisplayName();

		return pack.packId();
	}

	private static Optional<? extends ModContainer> findModNameFromModFile(PackResources pack) {
		if (pack.packId().startsWith("mod/")) {
			final String modId = pack.packId().substring("mod/".length());
			return ModList.get().getModContainerById(modId);
		}

		return Optional.empty();
	}

	@Override
	@Nullable
	public PatchedMetadata deriveMetadataFromMod(PackResources pack) {
		final Optional<? extends ModContainer> owningMod = findModNameFromModFile(pack);
		if (owningMod.isPresent()) {
			final ModContainer mod = owningMod.get();
			final Object obj = mod.getModInfo().getModProperties().get("patched");
			if (obj instanceof UnmodifiableConfig cfg)
				return PatchedMetadata.of(
						cfg,
						NightConfigOps.INSTANCE,
						mod.getModInfo().getDisplayName() + " (" + mod.getModInfo().getModId() + ")");
		}

		return null;
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
		return rl -> ResourceLocation.fromNamespaceAndPath(namespace, rl.getPath().substring(prefixLen + namespace.length() + 1));
	}
}