package nexusvault.archive;

public interface IdxFileLink extends IdxEntry {

	byte[] getShaHash();

	int getFlags();

	long getUncompressedSize();

	long getCompressedSize();

	String getFileEnding();

	String getNameWithoutFileEnding();

}