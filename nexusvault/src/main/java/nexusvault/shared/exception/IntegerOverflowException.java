package nexusvault.shared.exception;

public final class IntegerOverflowException extends NexusVaultException {

	private static final long serialVersionUID = -8602843792483449030L;

	public IntegerOverflowException() {
		super();
	}

	public IntegerOverflowException(String s) {
		super(s);
	}

	public IntegerOverflowException(String message, Throwable cause) {
		super(message, cause);
	}

	public IntegerOverflowException(Throwable cause) {
		super(cause);
	}

}