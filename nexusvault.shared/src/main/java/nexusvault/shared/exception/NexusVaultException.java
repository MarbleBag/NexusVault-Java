package nexusvault.shared.exception;

/**
 * Base exception for all NexusVault related exceptions.
 */
public class NexusVaultException extends RuntimeException {

	private static final long serialVersionUID = 5287804699640781565L;

	public NexusVaultException() {
		super();
	}

	public NexusVaultException(String s) {
		super(s);
	}

	public NexusVaultException(String message, Throwable cause) {
		super(message, cause);
	}

	public NexusVaultException(Throwable cause) {
		super(cause);
	}

}
