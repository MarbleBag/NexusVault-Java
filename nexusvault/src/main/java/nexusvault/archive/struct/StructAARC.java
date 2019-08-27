package nexusvault.archive.struct;

public final class StructAARC extends StructRootBlock {

	public StructAARC() {
		super();
	}

	public StructAARC(int signature, int version, int entryCount, int headerIdx) {
		super(signature, version, entryCount, headerIdx);
	}

}
