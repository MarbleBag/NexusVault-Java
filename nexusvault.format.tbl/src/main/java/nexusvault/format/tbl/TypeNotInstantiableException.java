package nexusvault.format.tbl;

public class TypeNotInstantiableException extends TableException {

	private static final long serialVersionUID = -6395082953314038043L;

	public TypeNotInstantiableException() {
		super();
	}

	public TypeNotInstantiableException(String s) {
		super(s);
	}

	public TypeNotInstantiableException(String message, Throwable cause) {
		super(message, cause);
	}

	public TypeNotInstantiableException(Throwable cause) {
		super(cause);
	}
}
