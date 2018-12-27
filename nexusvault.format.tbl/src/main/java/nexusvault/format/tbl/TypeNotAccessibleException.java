package nexusvault.format.tbl;

public class TypeNotAccessibleException extends TableException {

	private static final long serialVersionUID = -7565360667551590898L;

	public TypeNotAccessibleException() {
		super();
	}

	public TypeNotAccessibleException(String s) {
		super(s);
	}

	public TypeNotAccessibleException(String message, Throwable cause) {
		super(message, cause);
	}

	public TypeNotAccessibleException(Throwable cause) {
		super(cause);
	}
}
