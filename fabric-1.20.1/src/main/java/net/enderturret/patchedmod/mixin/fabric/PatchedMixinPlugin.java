package net.enderturret.patchedmod.mixin.fabric;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;

@Internal
public final class PatchedMixinPlugin implements IMixinConfigPlugin {

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith("MixinCompositePackResources"))
			return getMinecraftPatch() >= 2;

		if (mixinClassName.endsWith("FilePackResourcesAccess"))
			return getMinecraftPatch() < 2;

		if (mixinClassName.contains("fabric.api") && !FabricLoader.getInstance().isModLoaded("fabric-resource-loader-v0"))
			return false;

		return true;
	}

	private static int getMinecraftPatch() {
		final Version mcVersion = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion();
		return ((SemanticVersion) mcVersion).getVersionComponent(2);
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