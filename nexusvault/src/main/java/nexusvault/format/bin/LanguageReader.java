package nexusvault.format.bin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteArrayBinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.bin.struct.StructEntry;
import nexusvault.format.bin.struct.StructFileHeader;
import nexusvault.shared.Text;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class LanguageReader {
	private LanguageReader() {
	}

	public static LanguageDictionary read(byte[] data) {
		return read(new ByteArrayBinaryReader(data, ByteOrder.LITTLE_ENDIAN));
	}

	public static LanguageDictionary read(ByteBuffer data) {
		return read(new ByteBufferBinaryReader(data));
	}

	public static LanguageDictionary read(BinaryReader reader) {
		final var header = new StructFileHeader(reader);

		if (header.signature != StructFileHeader.SIGNATURE) {
			throw new SignatureMismatchException("bin", StructFileHeader.SIGNATURE, header.signature);
		}

		if (header.version != 4) {
			throw new VersionMismatchException("bin", 4, header.version);
		}

		final long readPostHeaderPosition = reader.position();

		LanguageDictionary.Locale locale;
		{
			reader.seek(Seek.BEGIN, readPostHeaderPosition + header.languageTagNameOffset);
			final String localeTagName = Text.readUTF16(reader, (header.languageTagNameLength - 1) * 2);

			reader.seek(Seek.BEGIN, readPostHeaderPosition + header.languageShortNameOffset);
			final String localeShortName = Text.readUTF16(reader, (header.languageShortNameLength - 1) * 2);

			reader.seek(Seek.BEGIN, readPostHeaderPosition + header.languageLongNameOffset);
			final String localeLongName = Text.readUTF16(reader, (header.languageLongNameLength - 1) * 2);

			locale = new LanguageDictionary.Locale((int) header.languageType, localeTagName, localeShortName, localeLongName);
		}

		final var entries = new HashMap<Integer, String>();
		{
			final var entryOffsets = new StructEntry[(int) header.entryCount];
			for (int i = 0; i < (int) header.entryCount; ++i) {
				entryOffsets[i] = new StructEntry(reader);
			}

			for (final var offset : entryOffsets) {
				reader.seek(Seek.BEGIN, readPostHeaderPosition + header.textOffset + offset.characterOffset * 2);
				final var text = Text.readNullTerminatedUTF16(reader);
				entries.put(offset.id, text);
			}
		}

		return new LanguageDictionary(locale, entries);
	}

}
