/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteArrayUtil;
import kreed.io.util.ByteBufferUtil;
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

	public DataHeader(int signature, int version) {
		this.signature = signature;
		this.version = version;
	}

	public DataHeader(ByteBuffer buffer) {
		this.signature = ByteBufferUtil.getInt32(buffer);
		this.version = ByteBufferUtil.getInt32(buffer);
	}

	public DataHeader(byte[] data, int offset) {
		this.signature = ByteArrayUtil.getInt32(data, offset, ByteOrder.LITTLE_ENDIAN);
		this.version = ByteArrayUtil.getInt32(data, offset + 4, ByteOrder.LITTLE_ENDIAN);
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
			throw new SignatureMismatchException("Unknown File", expected, getSignature());
		}
	}

	public void validateVersion(int expected) throws VersionMismatchException {
		if (!checkVersion(expected)) {
			throw new VersionMismatchException("Unknown File", expected, getVersion());
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
