package nexusvault.vault.archive;

import nexusvault.vault.VaultException;

public abstract class ArchiveException extends VaultException {

	public static final class ArchiveHashCollisionException extends ArchiveException {

		private static final long serialVersionUID = 1L;

		public ArchiveHashCollisionException(String s) {
			super(s);
		}

		public ArchiveHashCollisionException() {
			super();
		}

		public ArchiveHashCollisionException(String message, Throwable cause) {
			super(message, cause);
		}

		public ArchiveHashCollisionException(Throwable cause) {
			super(cause);
		}

	}

	public static final class ArchiveHashException extends ArchiveException {

		private static final long serialVersionUID = 1L;

		public ArchiveHashException() {
			super();
		}

		public ArchiveHashException(String s) {
			super(s);
		}

		public ArchiveHashException(String message, Throwable cause) {
			super(message, cause);
		}

		public ArchiveHashException(Throwable cause) {
			super(cause);
		}

	}

	public static final class ArchiveHashNotFoundException extends ArchiveException {

		private static final long serialVersionUID = 1L;

		public ArchiveHashNotFoundException() {
			super();
		}

		public ArchiveHashNotFoundException(String s) {
			super(s);
		}

		public ArchiveHashNotFoundException(String message, Throwable cause) {
			super(message, cause);
		}

		public ArchiveHashNotFoundException(Throwable cause) {
			super(cause);
		}

	}

	private static final long serialVersionUID = 1L;

	public ArchiveException() {
		super();
	}

	public ArchiveException(String s) {
		super(s);
	}

	public ArchiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiveException(Throwable cause) {
		super(cause);
	}

}
