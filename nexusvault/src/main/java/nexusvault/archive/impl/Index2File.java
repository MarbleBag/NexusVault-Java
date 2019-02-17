package nexusvault.archive.impl;

import java.util.Arrays;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.struct.StructAIDX;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructIndexFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.shared.exception.IntegerOverflowException;

public final class Index2File {

	protected StructIndexFile header;
	protected PackHeader[] packHeader;
	protected AIDX aidx;
	protected IdxDirectory rootDirectory;

	public Index2File(StructArchiveFile indexHeader, StructPackHeader[] packs, StructAIDX aidx) {
		// TODO Auto-generated constructor stub
	}

	public PackHeader getRootPackHeader() {
		return packHeader[aidx.rootPackHeaderIdx];
	}

	public PackHeader getPackHeader(long idx) {
		if ((idx < 0) || (idx > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException();
		}
		return packHeader[(int) idx];
	}

	public IdxDirectory getRootDirectory() {
		return rootDirectory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((aidx == null) ? 0 : aidx.hashCode());
		result = (prime * result) + ((header == null) ? 0 : header.hashCode());
		result = (prime * result) + Arrays.hashCode(packHeader);
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
		final Index2File other = (Index2File) obj;
		if (aidx == null) {
			if (other.aidx != null) {
				return false;
			}
		} else if (!aidx.equals(other.aidx)) {
			return false;
		}
		if (header == null) {
			if (other.header != null) {
				return false;
			}
		} else if (!header.equals(other.header)) {
			return false;
		}
		if (!Arrays.equals(packHeader, other.packHeader)) {
			return false;
		}
		return true;
	}

}