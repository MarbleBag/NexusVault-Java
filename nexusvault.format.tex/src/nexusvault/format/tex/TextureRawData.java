package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface TextureRawData {

	void copyTo(int srcPosition, byte[] dst, int dstPosition, int length);

	ByteBuffer createView(int offset, int length);

	ByteOrder getByteOrder();

	int getSizeOfDataInBytes();

}
