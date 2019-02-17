package nexusvault.archive;

public final class VaultUnpackException extends ArchiveException {

	private static final long serialVersionUID = -1L;

	public VaultUnpackException() {
		super();
	}

	public VaultUnpackException(String s) {
		super(s);
	}

	public VaultUnpackException(String message, Throwable cause) {
		super(message, cause);
	}

	public VaultUnpackException(Throwable cause) {
		super(cause);
	}

}
