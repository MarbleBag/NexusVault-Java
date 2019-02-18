package nexusvault.archive;

public final class IdxEntryNotAFileException extends IdxException {

	private static final long serialVersionUID = 1L;

	public IdxEntryNotAFileException() {
		super();
	}

	public IdxEntryNotAFileException(String s) {
		super(s);
	}

	public IdxEntryNotAFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdxEntryNotAFileException(Throwable cause) {
		super(cause);
	}

}
