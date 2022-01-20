package nexusvault.vault;

public final class VaultDisposedException extends VaultException {

	private static final long serialVersionUID = 1L;

	public VaultDisposedException() {
		super();
	}

	public VaultDisposedException(String s) {
		super(s);
	}

	public VaultDisposedException(String message, Throwable cause) {
		super(message, cause);
	}

	public VaultDisposedException(Throwable cause) {
		super(cause);
	}

}
