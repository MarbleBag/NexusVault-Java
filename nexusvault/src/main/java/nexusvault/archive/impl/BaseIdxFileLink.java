package nexusvault.archive.impl;

import java.nio.ByteBuffer;

import nexusvault.archive.IdxFileLink;

final class BaseIdxFileLink extends BaseIdxEntry implements IdxFileLink {

	protected final int flags;
	protected final long writeTime;
	protected final long uncompressedSize;
	protected final long compressedSize;
	protected final byte[] shaHash;
	protected final int unk1;

	public BaseIdxFileLink(BaseIdxDirectory parent, String name, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash, int unk1) {
		super(parent, name);
		this.flags = flags;
		this.writeTime = writeTime;
		this.uncompressedSize = uncompressedSize;
		this.compressedSize = compressedSize;
		this.shaHash = hash;
		this.unk1 = unk1;
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

	public long getWriteTime() {
		return writeTime;
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
	public ByteBuffer getData() {
		return getArchive().getData(this);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("IdxFileLink [flags=");
		builder.append(flags);
		builder.append(", writeTime=");
		builder.append(writeTime);
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

}
