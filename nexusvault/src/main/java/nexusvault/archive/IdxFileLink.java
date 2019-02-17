package nexusvault.archive;

import java.nio.ByteBuffer;

public interface IdxFileLink extends IdxEntry {

	byte[] getShaHash();

	int getFlags();

	long getUncompressedSize();

	long getCompressedSize();

	String getFileEnding();

	String getNameWithoutFileEnding();

	ByteBuffer getData();

	void setData(ByteBuffer data, int flags);

}