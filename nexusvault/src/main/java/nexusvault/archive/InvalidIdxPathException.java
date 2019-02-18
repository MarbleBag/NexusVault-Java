package nexusvault.archive;

public final class InvalidIdxPathException extends IdxException {

	private static final long serialVersionUID = 1L;

	public InvalidIdxPathException() {
		super();
	}

	public InvalidIdxPathException(String message) {
		super(message);
	}

	public InvalidIdxPathException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidIdxPathException(Throwable cause) {
		super(cause);
	}

}
