package nexusvault.archive.util;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public class DataHeader {
	private final int signature;
	private final int version;

	public DataHeader(BinaryReader reader) {
		signature = reader.readInt32();
		version = reader.readInt32();
		reader.seek(Seek.CURRENT, -8);
	}

	public String getSignatureAsString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(String.valueOf((char) (signature >> 24)));
		builder.append(String.valueOf((char) (signature >> 16)));
		builder.append(String.valueOf((char) (signature >> 8)));
		builder.append(String.valueOf((char) (signature >> 0)));
		return builder.toString();
	}

	public int getSignature() {
		return signature;
	}

	public int getVersion() {
		return version;
	}

	public boolean checkSignature(int expected) {
		return getSignature() == expected;
	}

	public boolean checkVersion(int expected) {
		return version == expected;
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
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + signature;
		result = (prime * result) + version;
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
		if (signature != other.signature) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
	}

}
