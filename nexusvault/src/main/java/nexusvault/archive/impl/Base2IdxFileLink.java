package nexusvault.archive.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;

import nexusvault.archive.IdxFileLink;
import nexusvault.archive.IdxPath;

@Deprecated
final class Base2IdxFileLink extends Base2IdxEntry implements IdxFileLink {

	protected final int flags;
	protected final long time;
	protected final long uncompressedSize;
	protected final long compressedSize;
	protected final byte[] shaHash;
	protected final int unk1;

	protected Base2IdxFileLink(Base2IdxDirectory parent, String name, int flags, long time, long uncompressedSize, long compressedSize, byte[] shaHash,
			int unk1) {
		super(parent, name);
		if (parent == null) {
			throw new IllegalArgumentException("'parent' must not be null.");
		}
		if (shaHash == null) {
			throw new IllegalArgumentException("'shaHash' must not be null.");
		}

		this.flags = flags;
		this.time = time;
		this.uncompressedSize = uncompressedSize;
		this.compressedSize = compressedSize;
		this.shaHash = new byte[shaHash.length];
		System.arraycopy(shaHash, 0, this.shaHash, 0, shaHash.length);
		this.unk1 = unk1;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("IdxFileLink [");
		builder.append("name=");
		builder.append(fullName());
		builder.append(", flags=");
		builder.append(flags);
		builder.append(", time=");
		builder.append(time);
		builder.append(", uncompressedSize=");
		builder.append(uncompressedSize);
		builder.append(", compressedSize=");
		builder.append(compressedSize);
		builder.append(", shaHash=");
		builder.append(ByteUtil.byteToHex(shaHash));
		builder.append(", unk1=");
		builder.append(unk1);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + (int) (compressedSize ^ (compressedSize >>> 32));
		result = (prime * result) + flags;
		result = (prime * result) + Arrays.hashCode(shaHash);
		result = (prime * result) + (int) (time ^ (time >>> 32));
		result = (prime * result) + (int) (uncompressedSize ^ (uncompressedSize >>> 32));
		result = (prime * result) + unk1;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Base2IdxFileLink other = (Base2IdxFileLink) obj;
		if (compressedSize != other.compressedSize) {
			return false;
		}
		if (flags != other.flags) {
			return false;
		}
		if (!Arrays.equals(shaHash, other.shaHash)) {
			return false;
		}
		if (time != other.time) {
			return false;
		}
		if (uncompressedSize != other.uncompressedSize) {
			return false;
		}
		if (unk1 != other.unk1) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] getShaHash() {
		return shaHash;
	}

	@Override
	public int getFlags() {
		return flags;
	}

	@Override
	public long getUncompressedSize() {
		return uncompressedSize;
	}

	@Override
	public long getCompressedSize() {
		return compressedSize;
	}

	@Override
	public String getFileEnding() {
		final int fileExtStart = getName().lastIndexOf(".");
		if (fileExtStart < 0) {
			return "";
		} else {
			return getName().substring(fileExtStart + 1);
		}
	}

	@Override
	public String getNameWithoutFileEnding() {
		final String name = getName();
		final int ext = name.lastIndexOf('.');
		if (ext < 0) {
			return name;
		} else {
			return name.substring(0, ext);
		}
	}

	@Override
	public IdxPath getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteBuffer getData() {
		// TODO Auto-generated method stub
		return null;
	}

}