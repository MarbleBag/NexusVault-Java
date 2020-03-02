package nexusvault.format.tex.jpg;

import nexusvault.format.tex.TextureEncodingException;

public class TextureJPGEncodingException extends TextureEncodingException {
	private static final long serialVersionUID = 1L;

	public TextureJPGEncodingException() {
		super();
	}

	public TextureJPGEncodingException(String s) {
		super(s);
	}

	public TextureJPGEncodingException(String message, Throwable cause) {
		super(message, cause);
	}

	public TextureJPGEncodingException(Throwable cause) {
		super(cause);
	}
}