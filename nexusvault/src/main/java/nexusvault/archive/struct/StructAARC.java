package nexusvault.archive.struct;

public final class StructAARC extends StructRootPackInfo {

	public static final int SIGNATURE_AARC = ('A' << 24) | ('A' << 16) | ('R' << 8) | 'C';

	public StructAARC() {
		super();
	}

	public StructAARC(int signature, int version, int entryCount, int headerIdx) {
		super(signature, version, entryCount, headerIdx);
	}

}
