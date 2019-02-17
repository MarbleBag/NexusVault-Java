package nexusvault.archive.impl;

import java.nio.ByteBuffer;

import kreed.io.util.BinaryReader;
import nexusvault.archive.VaultUnpackException;

@Deprecated
interface VaultUnpacker {
	ByteBuffer unpack(BinaryReader reader, long compressedSize, long uncompressedSize) throws VaultUnpackException;
}
