package nexusvault.pack.archive;

public class ArchiveEntryNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 5530467723975048345L;

	public ArchiveEntryNotFoundException() {
		super();
	}

	public ArchiveEntryNotFoundException(String s) {
		super(s);
	}

	public ArchiveEntryNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiveEntryNotFoundException(Throwable cause) {
		super(cause);
	}

}
