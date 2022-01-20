package nexusvault.util;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public class DataHeader {
	private final int signature;
	private final int version;

	public DataHeader(BinaryReader reader) {
		this.signature = reader.readInt32();
		this.version = reader.readInt32();
		reader.seek(Seek.CURRENT, -8);
	}

	public String getSignatureAsString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(String.valueOf((char) (this.signature >> 24)));
		builder.append(String.valueOf((char) (this.signature >> 16)));
		builder.append(String.valueOf((char) (this.signature >> 8)));
		builder.append(String.valueOf((char) (this.signature >> 0)));
		return builder.toString();
	}

	public int getSignature() {
		return this.signature;
	}

	public int getVersion() {
		return this.version;
	}

	public boolean checkSignature(int expected) {
		return getSignature() == expected;
	}

	public boolean checkVersion(int expected) {
		return this.version == expected;
	}

	public void validateSignature(int expected) throws SignatureMismatchException {
		if (!checkSignature(expected)) {
			throw new SignatureMismatchException("Unknown File", getSignature(), expected);
		}
	}

	public void validateVersion(int expected) throws VersionMismatchException {
		if (!checkVersion(expected)) {
			throw new VersionMismatchException("Unknown File", getVersion(), expected);
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DataHeader [signature=");
		builder.append(getSignatureAsString());
		builder.append(", version=");
		builder.append(this.version);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.signature;
		result = prime * result + this.version;
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
		final DataHeader other = (DataHeader) obj;
		if (this.signature != other.signature) {
			return false;
		}
		if (this.version != other.version) {
			return false;
		}
		return true;
	}

}
