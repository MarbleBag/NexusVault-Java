package nexusvault.pack.util;

import nexusvault.pack.index.IdxException;

public class InvalidIdxFilePathException extends IdxException {

	private static final long serialVersionUID = -1795902385893047235L;

	public InvalidIdxFilePathException() {
		super();
	}

	public InvalidIdxFilePathException(String message) {
		super(message);
	}

	public InvalidIdxFilePathException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidIdxFilePathException(Throwable cause) {
		super(cause);
	}

}
