package nexusvault.archive;

public abstract class IdxException extends RuntimeException {

	private static final long serialVersionUID = -1225134925914672097L;

	public IdxException() {
		super();
	}

	public IdxException(String s) {
		super(s);
	}

	public IdxException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdxException(Throwable cause) {
		super(cause);
	}

}
