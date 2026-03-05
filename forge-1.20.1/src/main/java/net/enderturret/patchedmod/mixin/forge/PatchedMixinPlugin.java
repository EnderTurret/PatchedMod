package net.enderturret.patchedmod.mixin.forge;

import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.fml.loading.LoadingModList;

/**
 * Patched's mixin config plugin.
 * @author EnderTurret
 */
@Internal
public final class PatchedMixinPlugin implements IMixinConfigPlugin {

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith("MixinCompositePackResources"))
			return getMinecraftPatch() >= 2;

		if (mixinClassName.endsWith("FilePackResourcesAccess"))
			return getMinecraftPatch() < 2;

		return true;
	}

	private static int getMinecraftPatch() {
		final ArtifactVersion mcVersion = LoadingModList.get().getModFileById("minecraft").getMods().get(0).getVersion();
		return mcVersion.getIncrementalVersion(); // major.minor.incremental
	}

	@Override
	public void onLoad(String mixinPackage) {}
	@Override
	public String getRefMapperConfig() { return null; }
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override
	public List<String> getMixins() { return null; }
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}