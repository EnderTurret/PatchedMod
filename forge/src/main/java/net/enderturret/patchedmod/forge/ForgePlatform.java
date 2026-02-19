package net.enderturret.patchedmod.forge;

import java.util.Optional;
import java.util.function.Function;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.VanillaPackResources;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

import net.enderturret.patchedmod.common.env.IPatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchedMetadata;
import net.enderturret.patchedmod.util.env.IPlatform;

final class ForgePlatform implements IPlatform {

	@Override
	public boolean isPhysicalClient() {
		return FMLEnvironment.getDist() == Dist.CLIENT;
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
	public String getName(IPatchingPackResources pack) {
		final Optional<? extends ModContainer> mod = findModNameFromModFile(pack);

		if (mod.isPresent())
			return "mod/" + mod.get().getModInfo().getDisplayName();

		return pack.patched$packId();
	}

	private static Optional<? extends ModContainer> findModNameFromModFile(IPatchingPackResources pack) {
		if (pack.patched$packId().startsWith("mod/")) {
			final String modId = pack.patched$packId().substring("mod/".length());
			return ModList.get().getModContainerById(modId);
		}

		return Optional.empty();
	}

	@Override
	@Nullable
	public PatchedMetadata deriveMetadataFromMod(IPatchingPackResources pack) {
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
	public boolean needsSwapNamespaceAndPath(IPatchingPackResources pack) {
		return true;
	}

	@Override
	public Function<Identifier, Identifier> getRenamer(IPatchingPackResources pack, String namespace) {
		final boolean vanilla = pack instanceof VanillaPackResources;
		final int prefixLen = 0;
		// PathPackResources:     :minecraft/something → minecraft:something
		// FilePackResources is handled separately.
		// VanillaPackResources:  :minecraft/something → minecraft:something
		return rl -> Identifier.fromNamespaceAndPath(namespace, rl.getPath().substring(prefixLen + namespace.length() + 1));
	}
}