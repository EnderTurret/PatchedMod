package net.enderturret.patchedmod.mixin.quilt;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

@Internal
public final class PatchedMixinPlugin implements IMixinConfigPlugin {

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.contains("quilt.api") && !QuiltLoader.isModLoaded("fabric-resource-loader-v0"))
			return false;

		return true;
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