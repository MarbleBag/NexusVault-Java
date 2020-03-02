package nexusvault.format.tex.jpg.tool.encoder;

public final class HuffmanEncoderFault extends RuntimeException {

	private static final long serialVersionUID = 6156097213710271118L;

	public HuffmanEncoderFault() {
		super();
	}

	public HuffmanEncoderFault(String type, String errorMessage) {
		super(type + " : " + errorMessage);
	}

	public HuffmanEncoderFault(String s) {
		super(s);
	}

	public HuffmanEncoderFault(String message, Throwable cause) {
		super(message, cause);
	}

	public HuffmanEncoderFault(Throwable cause) {
		super(cause);
	}

}
