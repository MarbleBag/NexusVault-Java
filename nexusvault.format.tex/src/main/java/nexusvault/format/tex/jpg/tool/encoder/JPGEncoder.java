package nexusvault.format.tex.jpg.tool.encoder;

import java.nio.ByteBuffer;

import kreed.io.util.BinaryWriter;
import kreed.io.util.ByteAlignmentUtil;
import kreed.io.util.ByteBufferBinaryWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.jpg.tool.Constants;

public final class JPGEncoder {

	private final JPGImageEncoder encoder;

	public JPGEncoder(TexType target, boolean[] ignoreChannel, int[] jpgQuality) {
		this.encoder = buildEncoder(target, ignoreChannel, jpgQuality);
	}

	private JPGImageEncoder buildEncoder(TexType texType, boolean[] ignoreChannel, int[] jpgQuality) {
		final int constantIdx = Constants.getArrayIndexForType(texType);

		final var layerHasDefault = new boolean[Constants.NUMBER_OF_LAYERS];
		final var quantTables = new float[Constants.NUMBER_OF_LAYERS][];
		final var layerType = Constants.TYPE_PER_LAYER[constantIdx];
		for (int i = 0; i < Constants.NUMBER_OF_LAYERS; ++i) {
			quantTables[i] = Constants.getAdjustedQuantTable(layerType[i], jpgQuality[i]);
			layerHasDefault[i] = ignoreChannel[i];
		}

		return new JPGImageEncoder(texType, layerHasDefault, quantTables);
	}

	public void encode(byte[] image, int imageWidth, int imageHeight, BinaryWriter out) {
		this.encoder.encode(image, imageWidth, imageHeight, out);
	}

	public ByteBuffer encode(byte[] image, int imageWidth, int imageHeight) {
		// estimated size
		final var writeBuffer = ByteBuffer.allocate(ByteAlignmentUtil.alignTo8Byte(imageWidth) * ByteAlignmentUtil.alignTo8Byte(imageHeight) * 4);
		final var writer = new ByteBufferBinaryWriter(writeBuffer);
		this.encode(image, imageWidth, imageHeight, writer);
		writer.flush();
		writeBuffer.flip();

		if (writeBuffer.capacity() == writeBuffer.remaining()) {
			return writeBuffer;
		}

		final var readBuffer = ByteBuffer.allocate(writeBuffer.remaining()); // allocate new buffer with the correct size
		readBuffer.put(writeBuffer).rewind();
		return readBuffer;
	}

}
