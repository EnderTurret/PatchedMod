package net.enderturret.patchedmod.mixin.dynamic_patches;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

import net.enderturret.patchedmod.common.env.PatchingPackResources;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

/**
 * Handles placing packs with dynamic patches into the relevant {@code FallbackResourceManager}s.
 * @author EnderTurret
 */
@Mixin(MultiPackResourceManager.class)
public abstract class MixinMultiPackResourceManager {

	// I'd use an @Inject here but old mixin in constructors moment.
	@ModifyExpressionValue(at = @At(value = "NEW", target = "java/util/HashMap"), method = "<init>")
	private HashMap<?, ?> patched$fetchNamespaces(HashMap<?, ?> original, PackType type, List<PackResources> packs, @Share("namespaces") LocalRef<Set<String>> namespaces) {
		final Set<String> set = new HashSet<>();

		for (PackResources pack : packs)
			set.addAll(pack.getNamespaces(type));

		namespaces.set(set);

		return original;
	}

	@ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/PackResources;getNamespaces(Lnet/minecraft/server/packs/PackType;)Ljava/util/Set;"), method = "<init>")
	private Set<String> patched$includeDynamicPacks(Set<String> original, PackType type, List<PackResources> packs, @Local(ordinal = 0, index = 5) PackResources pack, @Share("namespaces") LocalRef<Set<String>> namespaces) {
		if (pack instanceof PatchingPackResources ppp && ppp.patched$hasPatches()) {
			final PatchedPackType packType = PatchedPackType.fromVanilla(PackType.CLIENT_RESOURCES, type);
			Set<String> newSet = null;

			for (String ns : namespaces.get())
				if (ppp.patchedMetadata().hasDynamicNamespace(packType, ns)) {
					if (newSet == null) {
						newSet = new LinkedHashSet<>(original);
						original = newSet;
					}
					newSet.add(ns);
				}
		}

		return original;
	}
}