package nexusvault.shared.exception;

public class ExportException extends RuntimeException {

	private static final long serialVersionUID = -2168136828280107286L;

	public ExportException() {
		super();
	}

	public ExportException(String s) {
		super(s);
	}

	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExportException(Throwable cause) {
		super(cause);
	}

}
