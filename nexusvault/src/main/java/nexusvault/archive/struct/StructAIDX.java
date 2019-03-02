package nexusvault.archive.struct;

public final class StructAIDX extends StructRootPackInfo {
	public static final int SIGNATURE_AIDX = ('A' << 24) | ('I' << 16) | ('D' << 8) | 'X';

	public StructAIDX() {
		super();
	}

	public StructAIDX(int signature, int version, int entryCount, int headerIdx) {
		super(signature, version, entryCount, headerIdx);
	}

}