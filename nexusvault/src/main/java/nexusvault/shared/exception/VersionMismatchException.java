package nexusvault.shared.exception;

public class VersionMismatchException extends NexusVaultException {

	private static final long serialVersionUID = 5457680416401107519L;

	public VersionMismatchException() {
		super();
	}

	public VersionMismatchException(String s) {
		super(s);
	}

	public VersionMismatchException(String name, int expected, int actual) {
		this(name, String.valueOf(expected), String.valueOf(actual));
	}

	public VersionMismatchException(String name, String expected, String actual) {
		super(String.format("%s : Expected '%s', but was '%s'", name, expected, actual));
	}

	public VersionMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public VersionMismatchException(Throwable cause) {
		super(cause);
	}

}
