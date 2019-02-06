package nexusvault.archive;

public final class IdxEntryNotAFile extends IdxException {

	private static final long serialVersionUID = 5530467723975048345L;

	public IdxEntryNotAFile() {
		super();
	}

	public IdxEntryNotAFile(String s) {
		super(s);
	}

	public IdxEntryNotAFile(String message, Throwable cause) {
		super(message, cause);
	}

	public IdxEntryNotAFile(Throwable cause) {
		super(cause);
	}

}
