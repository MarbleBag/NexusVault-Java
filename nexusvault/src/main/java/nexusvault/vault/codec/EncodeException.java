package nexusvault.vault.codec;

import nexusvault.vault.VaultException;

public final class EncodeException extends VaultException {

	private static final long serialVersionUID = 1L;

	public EncodeException() {
		super();
	}

	public EncodeException(String s) {
		super(s);
	}

	public EncodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public EncodeException(Throwable cause) {
		super(cause);
	}

}
