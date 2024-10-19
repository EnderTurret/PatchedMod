package net.enderturret.patchedmod.mixin.fabric;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import net.enderturret.patchedmod.internal.command.PatchedCommand;
import net.enderturret.patchedmod.internal.env.IEnvironment;

@Mixin(Commands.class)
public abstract class MixinCommands {

	@Shadow
	@Final
	private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(at = @At("TAIL"), method = "<init>")
	private void patched$registerServerCommands(CallbackInfo ci) {
		dispatcher.register(PatchedCommand.create(new IEnvironment.ServerEnvironment()));
	}
}