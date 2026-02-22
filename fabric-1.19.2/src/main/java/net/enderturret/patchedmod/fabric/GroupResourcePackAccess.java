package net.enderturret.patchedmod.fabric;

import java.util.List;
import java.util.Map;

import net.minecraft.server.packs.PackResources;

public interface GroupResourcePackAccess {

	public List<? extends PackResources> patched$packs();
	public Map<String, List<PackResources>> patched$namespacedPacks();
}