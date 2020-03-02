package nexusvault.archive;

import nexusvault.shared.exception.NexusVaultException;

public class ArchiveException extends NexusVaultException {

	private static final long serialVersionUID = 1964425428196788467L;

	public ArchiveException() {
		super();
	}

	public ArchiveException(String s) {
		super(s);
	}

	public ArchiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiveException(Throwable cause) {
		super(cause);
	}

}
