package nexusvault.format.tex.jpg.tool.encoder;

public interface BitConsumer {

	void consume(int data, int numberOfBits);

	void endOfData();

}
