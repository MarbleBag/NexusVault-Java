package nexusvault.format.tex.jpg.tool.encoder;

import nexusvault.format.tex.jpg.tool.HuffmanTable;

public final class HuffmanEncoder {
	private HuffmanEncoder() {
	}

	private static void assertNotOutOfBounds(String argumentName, int value, int lowerBound, int upperBound) {
		if (value < lowerBound || upperBound < value) {
			throw new IllegalArgumentException(String.format("%s : %d is not within [%d; %d]", argumentName, value, lowerBound, upperBound));
		}
	}

	public static void encode(HuffmanTable dc, HuffmanTable ac, BitConsumer consumer, int[] src, int srcOffset, int srcLength) {
		if (dc == null) {
			throw new IllegalArgumentException("'dc' must not be null");
		}
		if (ac == null) {
			throw new IllegalArgumentException("'ac' must not be null");
		}
		if (consumer == null) {
			throw new IllegalArgumentException("'consumer' must not be null");
		}
		if (src == null) {
			throw new IllegalArgumentException("'dst' must not be null");
		}

		assertNotOutOfBounds("srcOffset", srcOffset, 0, src.length);
		assertNotOutOfBounds("srcLength", srcLength, 1, src.length);
		assertNotOutOfBounds("srcOffset+srcLength", srcOffset + srcLength, 0, src.length);

		final int dcValue = src[srcOffset];
		final int dcBits = calculateBitLength(dcValue);
		encode(dc, consumer, dcBits);

		final int dcDiff = convertToUnsigned(dcValue, dcBits);
		consumer.consume(dcDiff, dcBits);

		for (int i = 1; i < srcLength;) {

			int acBits = 0x00;

			if (src[i + srcOffset] == 0) { // count zeros
				int zeroCounter = 1;

				for (int j = i + 1; j < srcLength; ++j) {
					if (src[j + srcOffset] == 0) {
						zeroCounter += 1;
					} else {
						break;
					}
				}

				i += zeroCounter;

				if (i == srcLength) { // end of block
					encode(ac, consumer, 0x00);
					break;
				}

				while (zeroCounter >= 16) {
					encode(ac, consumer, 0xF0); // special code for 16 zeros
					zeroCounter -= 16;
				}

				// msb contains the number of zeros before the next ac value
				acBits |= zeroCounter << 4 & 0xF0;
			}

			final int acValue = src[i++ + srcOffset];
			final int acValueBits = calculateBitLength(acValue);

			if (acValueBits > 0xF) {
				throw new HuffmanEncoderFault("Overflow",
						String.format("AC bit length %d is greater than %d bits, which is not supported by this encoder.", acValueBits, 16));
			}

			// lsb contains the number of bits to read for the actual ac value
			acBits |= acValueBits & 0x0F;

			encode(ac, consumer, acBits);

			final int acValueConverted = convertToUnsigned(acValue, acValueBits);
			consumer.consume(acValueConverted, acValueBits);
		}
	}

	private static int calculateBitLength(int value) {
		if (value == 0) {
			return 0;
		}
		return Integer.SIZE - Integer.numberOfLeadingZeros(Math.abs(value));
	}

	private static int convertToUnsigned(int data, int nBits) {
		if (data < 0) {
			data = data - ((-1 << nBits) + 1);
		}
		return data;
	}

	private static void encode(HuffmanTable table, BitConsumer consumer, int value) {
		final var huffValue = table.encode(value);
		if (huffValue == null) {
			final String tableName = String.format("(%d,%d)", table.getDHT().destinationId, table.getDHT().tableClass);
			final String word = String.format("%2s", Integer.toBinaryString(value)).replaceAll(" ", "0");
			throw new HuffmanEncoderFault("Encoding not found", String.format("Table %s contains no encoding for word %s", tableName, word));
		}
		consumer.consume(huffValue.encodedWord, huffValue.encodedWordBitLength);
	}

}
