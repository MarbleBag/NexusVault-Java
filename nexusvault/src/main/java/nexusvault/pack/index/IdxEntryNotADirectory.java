package nexusvault.pack.index;

public class IdxEntryNotADirectory extends IdxException {

	private static final long serialVersionUID = 5530467723975048345L;

	public IdxEntryNotADirectory() {
		super();
	}

	public IdxEntryNotADirectory(String s) {
		super(s);
	}

	public IdxEntryNotADirectory(String message, Throwable cause) {
		super(message, cause);
	}

	public IdxEntryNotADirectory(Throwable cause) {
		super(cause);
	}

}
