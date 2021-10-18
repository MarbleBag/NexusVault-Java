package nexusvault.archive;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A {@link IdxEntry} which represents a link to a readable resource in an archive. This class provides functionality to access its linked resource as well as
 * its meta-data.
 */
public interface IdxFileLink extends IdxEntry {

	byte[] getHash();

	int getFlags();

	long getUncompressedSize();

	long getCompressedSize();

	String getFileEnding();

	String getNameWithoutFileExtension();

	/**
	 * Reads the linked resource in a thread safe manner into a {@link ByteBuffer}
	 *
	 * @return A {@linkplain ByteBuffer} containing all data this {@link IdxFileLink} represents
	 * @throws IOException
	 */
	ByteBuffer getData() throws IOException;

}