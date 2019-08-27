package nexusvault.archive;

public class PackException extends ArchiveException {

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