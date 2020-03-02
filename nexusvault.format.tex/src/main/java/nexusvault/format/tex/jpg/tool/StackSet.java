package nexusvault.format.tex.jpg.tool;

public final class StackSet {

	public final int[] data;
	private int idx;

	public StackSet(int blocks) {
		data = new int[blocks * Constants.BLOCK_SIZE];
	}

	public void setId(int id) {
		idx = id;
	}

	public int getId() {
		return idx;
	}

}
