package nexusvault.shared.exception;

public class SignatureMismatchException extends RuntimeException {

	private static final long serialVersionUID = -7254283632187562387L;

	public SignatureMismatchException() {
		super();
	}

	public SignatureMismatchException(String s) {
		super(s);
	}

	public SignatureMismatchException(String name, int expected, int actual) {
		super(String.format("%s : Expected '%s', but was '%s'", name, toString(expected), toString(actual)));
	}

	private static String toString(int expected) {
		return "" + ((char) (expected >> 24) & 0xFF) + ((char) (expected >> 16) & 0xFF) + ((char) (expected >> 8) & 0xFF) + ((char) (expected >> 0) & 0xFF);
	}

	public SignatureMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public SignatureMismatchException(Throwable cause) {
		super(cause);
	}

}