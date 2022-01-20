package nexusvault.vault.archive;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kreed.io.util.BinaryReader;
import nexusvault.vault.archive.ArchiveException.ArchiveHashException;

public final class Hash {
	private Hash() {
	}

	private static byte[] returnHash(MessageDigest md) throws ArchiveHashException {
		final byte[] hash = md.digest();
		if (hash.length != 20) {
			throw new ArchiveHashException(String.format("hash result needs to be exactly 20 bytes long, was %d", hash.length));
		}
		return hash;
	}

	public static byte[] computeHash(BinaryReader data) throws ArchiveHashException {
		try {
			final var md = MessageDigest.getInstance("SHA-1");
			while (!data.isEndOfData()) {
				md.update(data.readInt8());
			}
			return returnHash(md);
		} catch (final NoSuchAlgorithmException e) {
			throw new ArchiveHashException(e);
		}
	}

	public static byte[] computeHash(ByteBuffer data) throws ArchiveHashException {
		try {
			final var md = MessageDigest.getInstance("SHA-1");
			md.update(data);
			return returnHash(md);
		} catch (final NoSuchAlgorithmException e) {
			throw new ArchiveHashException(e);
		}
	}

	public static byte[] computeHash(byte[] data) throws ArchiveHashException {
		return computeHash(data, 0, data.length);
	}

	public static byte[] computeHash(byte[] data, int offset, int length) throws ArchiveHashException {
		try {
			final var md = MessageDigest.getInstance("SHA-1");
			md.update(data, offset, length);
			return returnHash(md);
		} catch (final NoSuchAlgorithmException e) {
			throw new ArchiveHashException(e);
		}
	}

}
