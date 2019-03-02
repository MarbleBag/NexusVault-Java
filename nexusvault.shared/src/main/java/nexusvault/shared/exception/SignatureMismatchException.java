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

	public static String toString(int signature) {
		final char a = (char) ((signature >> 0x18) & 0xFF);
		final char b = (char) ((signature >> 0x10) & 0xFF);
		final char c = (char) ((signature >> 0x08) & 0xFF);
		final char d = (char) ((signature >> 0x00) & 0xFF);
		return "" + a + b + c + d;
	}

	public SignatureMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public SignatureMismatchException(Throwable cause) {
		super(cause);
	}

}