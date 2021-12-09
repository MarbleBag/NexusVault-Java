package nexusvault.format.tex.jpg.old;

final class StackResourceHandler {
	private final int sizeInBlocks;

	public StackResourceHandler(int sizeInBlocks) {
		this.sizeInBlocks = sizeInBlocks;
	}

	public StackSet getFreeStack() {
		return new StackSet(this.sizeInBlocks);
	}

	public void returnStack(StackSet stack) {

	}
}