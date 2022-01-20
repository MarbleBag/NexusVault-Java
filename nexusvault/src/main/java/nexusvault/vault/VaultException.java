package nexusvault.vault;

import nexusvault.shared.exception.NexusVaultException;

/**
 * Base exception related to all exceptions thrown
 */
public class VaultException extends NexusVaultException {

	private static final long serialVersionUID = 1L;

	public VaultException() {
		super();
	}

	public VaultException(String s) {
		super(s);
	}

	public VaultException(String message, Throwable cause) {
		super(message, cause);
	}

	public VaultException(Throwable cause) {
		super(cause);
	}

}
