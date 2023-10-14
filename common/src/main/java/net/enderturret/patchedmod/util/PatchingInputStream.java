package net.enderturret.patchedmod.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.packs.resources.Resource.IoSupplier;

import net.enderturret.patched.audit.PatchAudit;

@SuppressWarnings("resource")
public class PatchingInputStream extends FilterInputStream {

	private PatchFunction patcher;

	@Nullable
	private PatchAudit audit = null;

	public PatchingInputStream(IoSupplier<InputStream> delegate, PatchFunction patcher) throws IOException {
		super(delegate.get());
		this.patcher = Objects.requireNonNull(patcher);
	}

	private void transform() {
		if (patcher != null) {
			in = patcher.patch(in, audit);
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

	public void withAudit(PatchAudit audit) {
		this.audit = audit;
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

	@FunctionalInterface
	public static interface PatchFunction {
		public InputStream patch(InputStream stream, @Nullable PatchAudit audit);
	}
}