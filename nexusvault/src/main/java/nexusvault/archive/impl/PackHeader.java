package nexusvault.archive.impl;

public final class PackHeader {

	private final long offset;
	private final long size;

	public PackHeader() {
		this(0, 0);
	}

	public PackHeader(long offset, long size) {
		this.offset = offset;
		this.size = size;
	}

	@Override
	public String toString() {
		return "PackDirectoryHeader [offset=" + offset + ", size=" + size + "]";
	}

	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (offset ^ (offset >>> 32));
		result = (prime * result) + (int) (size ^ (size >>> 32));
		return result;
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
		final PackHeader other = (PackHeader) obj;
		if (offset != other.offset) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}

}