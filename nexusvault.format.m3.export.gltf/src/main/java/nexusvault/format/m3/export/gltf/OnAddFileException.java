package nexusvault.format.m3.export.gltf;

import nexusvault.shared.exception.ExportException;

public final class OnAddFileException extends ExportException {

	private static final long serialVersionUID = -2047895574718079791L;

	public OnAddFileException() {
		super();
	}

	public OnAddFileException(String s) {
		super(s);
	}

	public OnAddFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public OnAddFileException(Throwable cause) {
		super(cause);
	}

}
