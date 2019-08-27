package nexusvault.archive;

public class PackMalformedException extends PackException {

	private static final long serialVersionUID = 1L;

	public PackMalformedException() {
		super();
	}

	public PackMalformedException(String s) {
		super(s);
	}

	public PackMalformedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PackMalformedException(Throwable cause) {
		super(cause);
	}

}