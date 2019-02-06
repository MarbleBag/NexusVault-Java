package nexusvault.format.bin;

import java.nio.ByteBuffer;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import kreed.reflection.struct.DataReadDelegator;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructReader;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class LanguageReader {

	public static class LanguageEntry {
		private final int id;
		private final String text;

		public LanguageEntry(int id, String text) {
			super();
			this.id = id;
			this.text = text;
		}

		public int getId() {
			return id;
		}

		public String getText() {
			return text;
		}

	}

	public LanguageDictionary read(ByteBuffer byteBuffer) {
		return read(new ByteBufferBinaryReader(byteBuffer));
	}

	public LanguageDictionary read(BinaryReader source) {
		final StructReader<BinaryReader> structBuilder = StructReader.build(StructFactory.build(),
				DataReadDelegator.build(new kreed.reflection.struct.reader.BinaryReader()), false);
		final StructFileHeader header = structBuilder.read(StructFileHeader.class, source);

		if (header.signature != StructFileHeader.SIGNATURE) {
			throw new SignatureMismatchException("language file", StructFileHeader.SIGNATURE, header.signature);
		}

		if (header.version < 4) {
			throw new VersionMismatchException("language file", 4, header.version);
		}

		final long postHeaderPosition = source.getPosition();

		source.seek(Seek.BEGIN, postHeaderPosition + header.languageTagNameOffset);
		final String languageTagName = TextUtil.extractUTF16(source, header.languageTagNameLength * 2);
		source.seek(Seek.BEGIN, postHeaderPosition + header.languageShortNameOffset);
		final String languageShortName = TextUtil.extractUTF16(source, header.languageShortNameLength * 2);
		source.seek(Seek.BEGIN, postHeaderPosition + header.languageLongtNameOffset);
		final String languageLongName = TextUtil.extractUTF16(source, header.languageLongNameLength * 2);

		if (header.entryCount > Integer.MAX_VALUE) {
			throw new IntegerOverflowException();
		}

		if (header.entryCount > Integer.MAX_VALUE) {
			throw new IntegerOverflowException();
		}

		source.seek(Seek.BEGIN, postHeaderPosition + header.entryOffset);
		final StructEntry[] offsetEntries = new StructEntry[(int) header.entryCount];

		for (int i = 0; i < offsetEntries.length; ++i) {
			offsetEntries[i] = structBuilder.read(StructEntry.class, source);
		}

		final LanguageEntry[] entries = new LanguageEntry[(int) header.entryCount];
		for (int i = 0; i < offsetEntries.length; ++i) {
			final StructEntry structEntry = offsetEntries[i];
			source.seek(Seek.BEGIN, postHeaderPosition + header.textOffset + (structEntry.characterOffset * 2));
			final String text = TextUtil.extractNullTerminatedUTF16(source);
			entries[i] = new LanguageEntry(structEntry.id, text);
		}

		return new PreloadedLanguageDictionary((int) header.languageType, languageTagName, languageLongName, languageShortName, entries);
	}

}
