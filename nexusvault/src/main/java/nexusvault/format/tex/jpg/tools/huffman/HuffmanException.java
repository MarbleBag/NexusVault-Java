package nexusvault.format.tex.jpg.tools.huffman;

public final class HuffmanException extends RuntimeException {

	private static final long serialVersionUID = 6156097213710271118L;

	public HuffmanException() {
		super();
	}

	public HuffmanException(String type, String errorMessage) {
		super(type + " : " + errorMessage);
	}

	public HuffmanException(String s) {
		super(s);
	}

	public HuffmanException(String message, Throwable cause) {
		super(message, cause);
	}

	public HuffmanException(Throwable cause) {
		super(cause);
	}

}
