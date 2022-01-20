package nexusvault.shared.exception;

/**
 * A field is considered 'padding' if it's value across all files does not change.
 */
public final class NotUsedForPaddingException extends StructException {

	private static final long serialVersionUID = -3460835862590178820L;

	public NotUsedForPaddingException() {
		super();
	}

	public NotUsedForPaddingException(String s) {
		super(s);
	}

	public NotUsedForPaddingException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotUsedForPaddingException(Throwable cause) {
		super(cause);
	}

}
