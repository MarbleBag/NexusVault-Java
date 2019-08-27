package nexusvault.archive.impl;

import java.util.Arrays;
import java.util.Objects;

public final class IdxFileAttribute {

	private String name;
	private int flags;
	private long writeTime;
	private long uncompressedSize;
	private long compressedSize;
	private byte[] hash;
	private int unk_034;

	public IdxFileAttribute(String name, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash, int unk_034) {
		super();

		if (name == null) {
			throw new IllegalArgumentException("'name' must not be null");
		}
		if (hash == null) {
			throw new IllegalArgumentException("'hash' must not be null");
		}

		this.name = name;
		this.flags = flags;
		this.writeTime = writeTime;
		this.uncompressedSize = uncompressedSize;
		this.compressedSize = compressedSize;
		this.hash = hash;
		this.unk_034 = unk_034;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IdxFileAttribute other = (IdxFileAttribute) obj;
		return (compressedSize == other.compressedSize) && (flags == other.flags) && Arrays.equals(hash, other.hash) && Objects.equals(name, other.name)
				&& (uncompressedSize == other.uncompressedSize) && (unk_034 == other.unk_034) && (writeTime == other.writeTime);
	}

	public long getCompressedSize() {
		return compressedSize;
	}

	public int getFlags() {
		return flags;
	}

	public byte[] getHash() {
		return hash;
	}

	public String getName() {
		return name;
	}

	public long getUncompressedSize() {
		return uncompressedSize;
	}

	public int getUnk_034() {
		return unk_034;
	}

	public long getWriteTime() {
		return writeTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(hash);
		result = (prime * result) + Objects.hash(compressedSize, flags, name, uncompressedSize, unk_034, writeTime);
		return result;
	}

	public void setCompressedSize(long compressedSize) {
		this.compressedSize = compressedSize;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	/**
	 * @param hash
	 *            the new hash
	 * @throws IllegalArgumentException
	 *             if <code>hash</code> is null
	 */
	public void setHash(byte[] hash) {
		if (hash == null) {
			throw new IllegalArgumentException("'hash' must not be null");
		}
		this.hash = hash;
	}

	/**
	 * @param name
	 *            the new name
	 * @throws IllegalArgumentException
	 *             if <code>name</code> is null
	 */
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("'name' must not be null");
		}
		this.name = name;
	}

	public void setUncompressedSize(long uncompressedSize) {
		this.uncompressedSize = uncompressedSize;
	}

	public void setUnk_034(int unk_034) {
		this.unk_034 = unk_034;
	}

	public void setWriteTime(long writeTime) {
		this.writeTime = writeTime;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("IdxFileAttribute [name=");
		builder.append(name);
		builder.append(", flags=");
		builder.append(flags);
		builder.append(", writeTime=");
		builder.append(writeTime);
		builder.append(", uncompressedSize=");
		builder.append(uncompressedSize);
		builder.append(", compressedSize=");
		builder.append(compressedSize);
		builder.append(", hash=");
		builder.append(Arrays.toString(hash));
		builder.append(", unk_034=");
		builder.append(unk_034);
		builder.append("]");
		return builder.toString();
	}

}
