package nexusvault.format.m3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import kreed.io.util.ByteArrayBinaryReader;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.InMemoryModel;
import nexusvault.format.m3.impl.ReferenceUpdater;
import nexusvault.format.m3.struct.StructM3Header;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class ModelReader {
	private ModelReader() {
	}

	private static final int VERSION = 100;

	public static Model read(byte[] data) {
		final var reader = new ByteArrayBinaryReader(data, ByteOrder.LITTLE_ENDIAN);
		final var signature = reader.readInt32();
		if (signature != StructM3Header.SIGNATURE) {
			throw new SignatureMismatchException("m3", StructM3Header.SIGNATURE, signature);
		}

		final var version = reader.readInt32();
		if (version != VERSION) {
			throw new VersionMismatchException("m3", VERSION, version);
		}

		final var tracker = new BytePositionTracker(0, data.length, ByteBuffer.wrap(data));
		final var entry = ReferenceUpdater.update(tracker, StructM3Header.class);
		return new InMemoryModel(entry, tracker);
	}

}
