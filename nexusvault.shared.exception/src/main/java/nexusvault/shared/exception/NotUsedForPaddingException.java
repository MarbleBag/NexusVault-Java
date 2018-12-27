package nexusvault.shared.exception;

public class NotUsedForPaddingException extends IllegalArgumentException {

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
