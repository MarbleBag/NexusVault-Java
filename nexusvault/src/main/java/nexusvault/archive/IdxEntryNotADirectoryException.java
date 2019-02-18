package nexusvault.archive;

public final class IdxEntryNotADirectoryException extends IdxException {

	private static final long serialVersionUID = 1L;

	public IdxEntryNotADirectoryException() {
		super();
	}

	public IdxEntryNotADirectoryException(String s) {
		super(s);
	}

	public IdxEntryNotADirectoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdxEntryNotADirectoryException(Throwable cause) {
		super(cause);
	}

}
