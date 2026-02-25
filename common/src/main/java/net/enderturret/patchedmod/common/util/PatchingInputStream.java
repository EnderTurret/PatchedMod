package net.enderturret.patchedmod.common.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.audit.PatchAudit;

/**
 * <p>
 * {@code PatchingInputStream} wraps another stream and allows patching it using a supplied 'patch function'.
 * This allows delaying the patching of the stream until it is used.
 * </p>
 * <p>
 * The main purpose of this class is to expose <i>some</i> implementation details to callers so they may customize the patching operation.
 * In particular, this allows filling in {@linkplain PatchAudit patch audits} or shutting off patching entirely, although the latter is <i>not</i> considered API.
 * </p>
 * @author EnderTurret
 */
public class PatchingInputStream extends FilterInputStream {

	private PatchFunction patcher;

	@Nullable
	private PatchAudit audit = null;

	private PatchTrace trace = PatchTrace.none();

	/**
	 * Constructs a new {@code PatchingInputStream}.
	 * @param delegate The stream to be patched.
	 * @param patcher The patch function to apply to the stream.
	 * @throws IOException If an I/O error occurs opening the stream.
	 */
	public PatchingInputStream(InputStream delegate, PatchFunction patcher) throws IOException {
		super(delegate);
		this.patcher = Objects.requireNonNull(patcher);
	}

	private void transform() {
		if (patcher != null) {
			in = patcher.patch(in, audit, trace);
			patcher = null;
		}
	}

	/**
	 * Completely disables patching of this file.
	 * <b>This is for internal use only</b>; there is literally no reason a mod should want to shut off patching files.
	 * Do <b>not</b> touch this method. Don't even <i>think</i> about touching it.
	 * The last thing I need to deal with are mods that intentionally sabotage this mod because they feel like it.
	 * @deprecated Certain objects may be vital to your success; do not destroy patching apparatus.
	 */
	@Internal
	@Deprecated
	public void _disablePatching() {
		patcher = null;
	}

	/**
	 * Attaches an audit to the patching operation, allowing it to be filled out when the file is patched.
	 * @param audit The audit to fill out.
	 */
	public void withAudit(PatchAudit audit) {
		this.audit = audit;
	}

	/**
	 * Attaches a {@link PatchTrace} to the patching operation, allowing it to be filled out when the file is patched.
	 * @param trace The trace to fill out.
	 */
	public void withTrace(PatchTrace trace) {
		this.trace = Objects.requireNonNull(trace);
	}

	@Override
	public int read() throws IOException {
		transform();
		return super.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		transform();
		return super.read(b);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		transform();
		return super.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		transform();
		return super.skip(n);
	}

	@Override
	public int available() throws IOException {
		transform();
		return super.available();
	}

	@Override
	public synchronized void mark(int readlimit) {
		transform();
		super.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		transform();
		super.reset();
	}

	@Override
	public boolean markSupported() {
		transform();
		return super.markSupported();
	}

	/**
	 * Represents a function that may be applied to transform the contents of an {@link InputStream}.
	 * @author EnderTurret
	 */
	@FunctionalInterface
	public static interface PatchFunction {

		/**
		 * Applies the patch function to the specified stream, optionally with the specified audit.
		 * @param stream The stream to patch the contents of.
		 * @param audit The audit. May be {@code null}.
		 * @param trace The file trace. Used for the {@code /patched trace} command.
		 * @return The patched stream.
		 */
		public InputStream patch(InputStream stream, @Nullable PatchAudit audit, PatchTrace trace);
	}
}