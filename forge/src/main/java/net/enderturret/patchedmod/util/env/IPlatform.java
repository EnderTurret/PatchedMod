package net.enderturret.patchedmod.util.env;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;

@Internal
public interface IPlatform {

	public Logger logger();

	public boolean isPhysicalClient();
	public boolean isModLoaded(String modId);
}