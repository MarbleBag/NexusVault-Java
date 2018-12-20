package nexusvault.format.tbl;

public class TypeConversionException extends TableException {

	private static final long serialVersionUID = 598292434798942661L;

	public TypeConversionException() {
		super();
	}

	public TypeConversionException(String s) {
		super(s);
	}

	public TypeConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TypeConversionException(Throwable cause) {
		super(cause);
	}
}
