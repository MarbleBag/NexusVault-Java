package nexusvault.pack.archive;

public class ArchiveException extends RuntimeException {

	private static final long serialVersionUID = 3012161402564991003L;

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
