package nexusvault.archive.struct;

public final class StructAIDX extends StructRootBlock {

	public StructAIDX() {
		super();
	}

	public StructAIDX(int signature, int version, int entryCount, int headerIdx) {
		super(signature, version, entryCount, headerIdx);
	}

}