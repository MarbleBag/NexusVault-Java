package nexusvault.format.bin;

import java.nio.ByteBuffer;
import java.util.Objects;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import kreed.reflection.struct.DataReadDelegator;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructReader;
import nexusvault.format.bin.struct.StructEntry;
import nexusvault.format.bin.struct.StructFileHeader;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class LanguageReader {

	public StructFileHeader readHeader(ByteBuffer byteBuffer) {
		return readHeader(new ByteBufferBinaryReader(byteBuffer));
	}

	public StructFileHeader readHeader(BinaryReader source) {
		final StructReader<BinaryReader> structBuilder = StructReader.build(StructFactory.build(),
				DataReadDelegator.build(new kreed.reflection.struct.reader.BinaryReader()), false);
		final StructFileHeader header = structBuilder.read(StructFileHeader.class, source);

		if (header.signature != StructFileHeader.SIGNATURE) {
			throw new SignatureMismatchException("language file", StructFileHeader.SIGNATURE, header.signature);
		}

		if (header.version != 4) {
			throw new VersionMismatchException("language file", 4, header.version);
		}

		return header;
	}

	public LanguageDictionary read(ByteBuffer byteBuffer) {
		return read(new ByteBufferBinaryReader(byteBuffer));
	}

	public LanguageDictionary read(BinaryReader source) {
		return read(source, new DictionaryConfig());
	}

	public LanguageDictionary read(ByteBuffer byteBuffer, DictionaryConfig config) {
		return read(new ByteBufferBinaryReader(byteBuffer), config);
	}

	public LanguageDictionary read(BinaryReader source, DictionaryConfig config) {
		Objects.requireNonNull(source, "'source' must not be null");
		Objects.requireNonNull(config, "'config' must not be null");

		if (source.getPosition() != 0) {
			throw new IllegalArgumentException("Position of 'source' needs to be 0");
		}

		final StructFileHeader header = readHeader(source);

		if (header.entryCount > Integer.MAX_VALUE) {
			throw new IntegerOverflowException();
		}

		LanguageDictionary dictionary;
		if (config.getLazyLoading()) {
			dictionary = createLazyDictionary(header, source);
			if (config.getCaching()) {
				dictionary = new CachedLanguageDictionary(dictionary);
			}
		} else {
			dictionary = createPreloadedDictionary(header, source);
		}

		return dictionary;
	}

	private LanguageDictionary createLazyDictionary(StructFileHeader header, BinaryReader source) {
		final String localeTagName = getLocaleTag(source, header);
		final String localeShortName = getLocaleShort(source, header);
		final String localeLongName = getLocaleLong(source, header);

		return new InMemoryLanguageDictionary(header, source, localeTagName, localeShortName, localeLongName);
	}

	private LanguageDictionary createPreloadedDictionary(StructFileHeader header, BinaryReader source) {
		source.seek(Seek.BEGIN, StructFileHeader.SIZE_IN_BYTES + header.entryOffset);
		final StructEntry[] offsetEntries = new StructEntry[(int) header.entryCount];

		for (int i = 0; i < offsetEntries.length; ++i) {
			offsetEntries[i] = new StructEntry(source.readInt32(), source.readInt32());
		}

		final LanguageEntry[] entries = new LanguageEntry[(int) header.entryCount];
		for (int i = 0; i < offsetEntries.length; ++i) {
			final StructEntry structEntry = offsetEntries[i];
			source.seek(Seek.BEGIN, StructFileHeader.SIZE_IN_BYTES + header.textOffset + structEntry.characterOffset * 2);
			final String text = TextUtil.extractNullTerminatedUTF16(source);
			entries[i] = new PreloadedLanguageEntry(structEntry.id, text);
		}

		final String localeTagName = getLocaleTag(source, header);
		final String localeShortName = getLocaleShort(source, header);
		final String localeLongName = getLocaleLong(source, header);

		return new PreloadedLanguageDictionary((int) header.languageType, localeTagName, localeShortName, localeLongName, entries);
	}

	private String getLocaleLong(BinaryReader source, final StructFileHeader header) {
		source.seek(Seek.BEGIN, StructFileHeader.SIZE_IN_BYTES + header.languageLongtNameOffset);
		final String localeLongName = TextUtil.extractUTF16(source, (header.languageLongNameLength - 1) * 2);
		return localeLongName;
	}

	private String getLocaleShort(BinaryReader source, final StructFileHeader header) {
		source.seek(Seek.BEGIN, StructFileHeader.SIZE_IN_BYTES + header.languageShortNameOffset);
		final String localeShortName = TextUtil.extractUTF16(source, (header.languageShortNameLength - 1) * 2);
		return localeShortName;
	}

	private String getLocaleTag(BinaryReader source, final StructFileHeader header) {
		source.seek(Seek.BEGIN, StructFileHeader.SIZE_IN_BYTES + header.languageTagNameOffset);
		final String localeTagName = TextUtil.extractUTF16(source, (header.languageTagNameLength - 1) * 2);
		return localeTagName;
	}

}
