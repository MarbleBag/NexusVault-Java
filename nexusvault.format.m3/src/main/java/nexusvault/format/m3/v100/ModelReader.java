package nexusvault.format.m3.v100;

import java.nio.ByteBuffer;

import nexusvault.format.m3.Model;
import nexusvault.format.m3.v100.struct.StructM3Header;

public final class ModelReader {

	public static final String STR_SIGNATURE = "MODL";
	public static final int SIGNATURE = ('M' << 24) | ('O' << 16) | ('D' << 8) | 'L';

	public int getFileSignature() {
		return SIGNATURE;
	}

	public boolean acceptFileSignature(int signature) {
		return getFileSignature() == signature;
	}

	public boolean acceptFileVersion(int version) {
		return version == 100;
	}

	public Model read(ByteBuffer buffer) {
		final DataTracker fileReader = new DataTracker(buffer.position(), buffer.remaining(), buffer);

		final ReferenceUpdater updater = new ReferenceUpdater();
		final StructM3Header header = updater.start(fileReader, StructM3Header.class);

		return new InMemoryModel(header, fileReader);
	}

}
