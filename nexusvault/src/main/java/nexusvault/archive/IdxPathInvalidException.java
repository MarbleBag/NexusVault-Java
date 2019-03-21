package nexusvault.archive;

public final class IdxPathInvalidException extends IdxException {

	private static final long serialVersionUID = 1L;

	public IdxPathInvalidException() {
		super();
	}

	public IdxPathInvalidException(String message) {
		super(message);
	}

	public IdxPathInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdxPathInvalidException(Throwable cause) {
		super(cause);
	}

}
