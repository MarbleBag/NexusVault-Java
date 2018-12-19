package nexusvault.pack.index;

public class IdxEntryNotFound extends IdxException {

	private static final long serialVersionUID = -426104938072426214L;

	public IdxEntryNotFound() {
		super();
	}

	public IdxEntryNotFound(String s) {
		super(s);
	}

	public IdxEntryNotFound(String message, Throwable cause) {
		super(message, cause);
	}

	public IdxEntryNotFound(Throwable cause) {
		super(cause);
	}

}
