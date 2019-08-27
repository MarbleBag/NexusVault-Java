package nexusvault.archive;

public final class IdxEntryNotFoundException extends IdxException {

	private static final long serialVersionUID = 1L;

	public IdxEntryNotFoundException() {
		super();
	}

	public IdxEntryNotFoundException(String s) {
		super(s);
	}

	public IdxEntryNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdxEntryNotFoundException(Throwable cause) {
		super(cause);
	}

}
