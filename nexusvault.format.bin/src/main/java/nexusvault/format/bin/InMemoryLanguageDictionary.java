package nexusvault.format.bin;

import java.util.Collection;
import java.util.Iterator;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.bin.LanguageReader.LanguageEntry;

// TODO Lazy loader with cache
class InMemoryLanguageDictionary implements LanguageDictionary {

	private final StructFileHeader header;
	private final BinaryReader source;
	private final long position;

	private final String localeTag;
	private final String localeShort;
	private final String localeLong;

	public InMemoryLanguageDictionary(StructFileHeader header, BinaryReader source) {
		if (header == null) {
			throw new IllegalArgumentException("'header' must not be null");
		}
		if (header == source) {
			throw new IllegalArgumentException("'source' must not be null");
		}

		this.header = header;
		this.source = source;
		this.position = source.getPosition();

		final long postHeaderPosition = source.getPosition();

		source.seek(Seek.BEGIN, postHeaderPosition + header.languageTagNameOffset);
		this.localeTag = TextUtil.extractUTF16(source, header.languageTagNameLength * 2);
		source.seek(Seek.BEGIN, postHeaderPosition + header.languageShortNameOffset);
		this.localeShort = TextUtil.extractUTF16(source, header.languageShortNameLength * 2);
		source.seek(Seek.BEGIN, postHeaderPosition + header.languageLongtNameOffset);
		this.localeLong = TextUtil.extractUTF16(source, header.languageLongNameLength * 2);
	}

	@Override
	public Iterator<LanguageEntry> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long entryCount() {
		return header.entryCount;
	}

	@Override
	public Collection<Integer> getAllTextIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasText(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLocaleTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocaleLong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocaleShort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocaleType() {
		// TODO Auto-generated method stub
		return 0;
	}

}
