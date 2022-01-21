package nexusvault.vault.pack;

import nexusvault.vault.VaultException;

public abstract class PackException extends VaultException {

	/**
	 * Indicates that a pack is malformed
	 */
	public static class PackMalformedException extends PackException {

		private static final long serialVersionUID = 1L;

		public PackMalformedException() {
			super();
		}

		public PackMalformedException(String s) {
			super(s);
		}

		public PackMalformedException(String message, Throwable cause) {
			super(message, cause);
		}

		public PackMalformedException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * Indicates that two different entries point to the same memory area
	 */
	public static final class PackIndexCollisionException extends PackMalformedException {

		private static final long serialVersionUID = 1L;

		public final int firstIndex;
		public final int lastIndex;
		public final long offset;

		public PackIndexCollisionException(int first, int last, long offset) {
			super(String.format("Index collision of %d and %d at %d", first, last, offset));
			this.firstIndex = first;
			this.lastIndex = last;
			this.offset = offset;
		}

	}

	public static final class PackIndexInvalidException extends PackException {

		private static final long serialVersionUID = 1L;
		public final long index;

		public PackIndexInvalidException(long index) {
			super("index: " + index);
			this.index = index;
		}

		public PackIndexInvalidException(long index, String msg) {
			super(String.format("index: %s - %s", index, msg));
			this.index = index;
		}

	}

	private static final long serialVersionUID = 1L;

	public PackException() {
		super();
	}

	public PackException(String s) {
		super(s);
	}

	public PackException(String message, Throwable cause) {
		super(message, cause);
	}

	public PackException(Throwable cause) {
		super(cause);
	}

}