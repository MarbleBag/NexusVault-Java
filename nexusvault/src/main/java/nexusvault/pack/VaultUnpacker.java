package nexusvault.pack;

import java.nio.ByteBuffer;

import kreed.io.util.BinaryReader;

interface VaultUnpacker {
	ByteBuffer unpack(BinaryReader reader, long compressedSize, long uncompressedSize) throws VaultUnpackException;
}
