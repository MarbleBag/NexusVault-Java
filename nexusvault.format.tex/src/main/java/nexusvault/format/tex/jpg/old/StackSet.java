package nexusvault.format.tex.jpg.old;

import nexusvault.format.tex.jpg.tool.Constants;

final class StackSet {

	public final int[] data;
	private int idx;

	public StackSet(int blocks) {
		this.data = new int[blocks * Constants.BLOCK_SIZE];
	}

	public void setId(int id) {
		this.idx = id;
	}

	public int getId() {
		return this.idx;
	}

}
