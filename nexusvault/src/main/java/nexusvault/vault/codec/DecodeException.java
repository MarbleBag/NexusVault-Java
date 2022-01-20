package nexusvault.vault.codec;

import nexusvault.vault.VaultException;

public final class DecodeException extends VaultException {

	private static final long serialVersionUID = 1L;

	public DecodeException() {
		super();
	}

	public DecodeException(String s) {
		super(s);
	}

	public DecodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DecodeException(Throwable cause) {
		super(cause);
	}

}
