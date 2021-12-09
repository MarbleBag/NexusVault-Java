package nexusvault.format.tbl;

import nexusvault.shared.exception.NexusVaultException;

public class TableException extends NexusVaultException {

	private static final long serialVersionUID = -8868653050581508889L;

	public TableException() {
		super();
	}

	public TableException(String s) {
		super(s);
	}

	public TableException(String message, Throwable cause) {
		super(message, cause);
	}

	public TableException(Throwable cause) {
		super(cause);
	}
}
