package nexusvault.archive;

public final class ArchiveHashCollisionException extends ArchiveException {

	private static final long serialVersionUID = 5530467723975048345L;

	public ArchiveHashCollisionException() {
		super();
	}

	public ArchiveHashCollisionException(String s) {
		super(s);
	}

	public ArchiveHashCollisionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiveHashCollisionException(Throwable cause) {
		super(cause);
	}

}
