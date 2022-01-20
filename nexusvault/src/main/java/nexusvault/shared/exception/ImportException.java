package nexusvault.shared.exception;

public class ImportException extends NexusVaultException {

	private static final long serialVersionUID = -2168136828280107286L;

	public ImportException() {
		super();
	}

	public ImportException(String s) {
		super(s);
	}

	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImportException(Throwable cause) {
		super(cause);
	}

}
