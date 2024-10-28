package net.enderturret.patchedmod.internal.flow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.enderturret.patched.JsonDocument;
import net.enderturret.patchedmod.internal.PatchedInternal;

/**
 * A class that wraps an {@link InputStream} in such a way that we can avoid reading from it if no patching is performed.
 * @author EnderTurret
 */
final class LazyPatchingWrapper {

	private InputStream stream;
	private byte[] oldBytes;
	private JsonDocument doc;

	public LazyPatchingWrapper(InputStream stream) {
		this.stream = stream;
	}

	public InputStream getOrCreateStream() {
		if (oldBytes == null) return Objects.requireNonNull(stream);

		if (doc != null)
			oldBytes = PatchedInternal.GSON.toJson(doc.getRoot()).getBytes(StandardCharsets.UTF_8);

		return new ByteArrayInputStream(oldBytes);
	}

	public JsonDocument get() {
		if (doc == null)
			doc = new JsonDocument(read());

		return doc;
	}

	private JsonElement read() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			stream.transferTo(baos);
			stream.close();
			stream = null;
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to transfer data to byte array", e);
		}

		oldBytes = baos.toByteArray();

		String json = new String(oldBytes, StandardCharsets.UTF_8);

		try {
			return JsonParser.parseString(json);
		} catch (Exception e) {
			throw new BailException(e);
		}
	}
}