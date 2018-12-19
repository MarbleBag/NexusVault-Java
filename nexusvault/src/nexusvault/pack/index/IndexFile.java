package nexusvault.pack.index;

import nexusvault.pack.PackHeader;
import nexusvault.shared.exception.IntegerOverflowException;

public final class IndexFile {

	protected StructIndexFileHeader header;
	protected PackHeader[] packHeader;
	protected AIDX aidx;
	protected IdxDirectory rootDirectory;

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

}