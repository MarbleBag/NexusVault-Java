package nexusvault.format.tex.jpg;

public class HuffmanDecoderFault extends RuntimeException {

	private static final long serialVersionUID = -716158335534284967L;

	public HuffmanDecoderFault() {
		super();
	}

	public HuffmanDecoderFault(String type, String errorMessage) {
		super(type + " : " + errorMessage);
	}

	public HuffmanDecoderFault(String s) {
		super(s);
	}

	public HuffmanDecoderFault(String message, Throwable cause) {
		super(message, cause);
	}

	public HuffmanDecoderFault(Throwable cause) {
		super(cause);
	}

}
