package nexusvault.shared.exception;

/**
 * Thrown to indicate that a structure (created by hand or more so created by parsing a WS related file) has data that deviates from the expectation and needs
 * to be reviewed.
 */
public class StructException extends NexusVaultException {

	/**
	 *
	 */
	private static final long serialVersionUID = -6162209104779255483L;

	public StructException() {
		super();
	}

	public StructException(String s) {
		super(s);
	}

	public StructException(String message, Throwable cause) {
		super(message, cause);
	}

	public StructException(Throwable cause) {
		super(cause);
	}

}
