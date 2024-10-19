package net.enderturret.patchedmod;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.packs.PackType;

import net.enderturret.patched.ITestEvaluator;

/**
 * Represents the root test evaluator, which handles delegating to each registered condition.
 * @author EnderTurret
 */
public interface RootEvaluator extends ITestEvaluator {

	/**
	 * Returns the pack type of the evaluator.
	 * May be {@code null} if the evaluator was configured to be type-agnostic.
	 * @return The pack type.
	 */
	@Nullable
	public PackType packType();
}