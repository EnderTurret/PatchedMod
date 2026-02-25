package net.enderturret.patchedmod.common.internal.flow;

/**
 * An exception thrown in file patching to bail out of patch processing and give the caller the original data.
 * The primary circumstance that causes this exception to be thrown is parsing malformed JSON; it is preferable
 * for the caller to deal with this problem as they would need to when Patched isn't installed.
 * @author EnderTurret
 */
final class BailException extends RuntimeException {

	public BailException() {}
	public BailException(Throwable cause) { super(cause); }
}