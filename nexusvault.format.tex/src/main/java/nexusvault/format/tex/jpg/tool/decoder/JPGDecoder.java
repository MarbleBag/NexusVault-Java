package nexusvault.format.tex.jpg.tool.decoder;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.jpg.tool.Constants;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class JPGDecoder {

	private final JPGImageDecoder decoder;

	public JPGDecoder(StructTextureFileHeader header) {
		decoder = buildDecoder(header);
	}

	public byte[] decode(BinaryReader input, /* int byteLength, */ int width, int height) {
		return decoder.decode(input, /* byteLength, */ width, height);
	}

	private JPGImageDecoder buildDecoder(StructTextureFileHeader header) {
		final var texType = TexType.resolve(header);
		final int constantIdx = Constants.getArrayIndexForType(texType);

		final var layerHasDefault = new boolean[Constants.NUMBER_OF_LAYERS];
		final var layerDefault = new byte[Constants.NUMBER_OF_LAYERS];
		final var quantTables = new float[Constants.NUMBER_OF_LAYERS][];
		final var layerType = Constants.TYPE_PER_LAYER[constantIdx];
		for (int i = 0; i < Constants.NUMBER_OF_LAYERS; ++i) {
			final var layerInfo = header.layerInfos[i];
			quantTables[i] = Constants.getAdjustedQuantTable(layerType[i], layerInfo.getQuality());
			layerHasDefault[i] = layerInfo.hasReplacement();
			if (layerHasDefault[i]) {
				layerDefault[i] = layerInfo.getReplacement();
			}
		}

		return new JPGImageDecoder(texType, layerHasDefault, layerDefault, quantTables);
	}

}
