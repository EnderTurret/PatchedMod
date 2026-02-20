package net.enderturret.patchedmod.fabric.env;

import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.enderturret.patchedmod.common.env.PatchedMutableComponent;
import net.enderturret.patchedmod.common.internal.env.IEnvironment;

public abstract class AbstractEnvironment<T> implements IEnvironment<T> {

	@Override
	public Class<?> getResourceLocationClass() {
		return ResourceLocation.class;
	}

	@Override
	public ArgumentType<?> getResourceLocationArgumentType() {
		return ResourceLocationArgument.id();
	}

	@Override
	public PatchedMutableComponent translate(String languageKey, String message, Object... args) {
		return new ComponentWrapper(ComponentWrapper.translate(languageKey, message, args));
	}

	@Override
	public PatchedMutableComponent literalText(String text) {
		return new ComponentWrapper(Component.literal(text));
	}
}