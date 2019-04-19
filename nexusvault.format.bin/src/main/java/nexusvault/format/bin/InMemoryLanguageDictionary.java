package nexusvault.format.bin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.bin.struct.StructEntry;
import nexusvault.format.bin.struct.StructFileHeader;

final class InMemoryLanguageDictionary implements LanguageDictionary {

	private final StructFileHeader header;
	private final BinaryReader source;

	private final String localeTag;
	private final String localeShort;
	private final String localeLong;
	private Map<Integer, StructEntry> index;

	public InMemoryLanguageDictionary(StructFileHeader header, BinaryReader source, String localeTag, String localeShort, String localeLong) {
		if (header == null) {
			throw new IllegalArgumentException("'header' must not be null");
		}
		if (source == null) {
			throw new IllegalArgumentException("'source' must not be null");
		}
		if ((localeTag == null) || localeTag.isBlank()) {
			throw new IllegalArgumentException("'localeTag' must not be null or empty");
		}
		if ((localeShort == null) || localeShort.isBlank()) {
			throw new IllegalArgumentException("'localeShort' must not be null or empty");
		}
		if ((localeLong == null) || localeLong.isBlank()) {
			throw new IllegalArgumentException("'localeLong' must not be null or empty");
		}

		this.header = header;
		this.source = source;
		this.localeTag = localeTag;
		this.localeShort = localeShort;
		this.localeLong = localeLong;

		buildIndex();
	}

	private void buildIndex() {
		index = new HashMap<>();
		source.seek(Seek.BEGIN, StructFileHeader.SIZE_IN_BYTES + header.entryOffset);
		for (int i = 0; i < (int) header.entryCount; ++i) {
			final var entry = new StructEntry(source.readInt32(), source.readInt32());
			index.put(entry.id, entry);
		}
	}

	private String extractText(StructEntry entry) {
		source.seek(Seek.BEGIN, StructFileHeader.SIZE_IN_BYTES + header.textOffset + (entry.characterOffset * 2));
		final String text = TextUtil.extractNullTerminatedUTF16(source);
		return text;
	}

	@Override
	public Iterator<LanguageEntry> iterator() {
		return new Iterator<>() {

			final Iterator<Entry<Integer, StructEntry>> itr = index.entrySet().iterator();

			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public LanguageEntry next() {
				return new LanguageEntry() {

					final StructEntry entry = itr.next().getValue();

					@Override
					public String getText() {
						return extractText(entry);
					}

					@Override
					public int getId() {
						return entry.id;
					}
				};
			}
		};
	}

	@Override
	public long entryCount() {
		return header.entryCount;
	}

	@Override
	public Collection<Integer> getAllTextIds() {
		return Collections.unmodifiableSet(index.keySet());
	}

	@Override
	public String getText(int id) {
		final StructEntry entry = index.get(Integer.valueOf(id));
		return entry == null ? null : extractText(entry);
	}

	@Override
	public boolean hasText(int id) {
		return index.containsKey(Integer.valueOf(id));
	}

	@Override
	public String getLocaleTag() {
		return localeTag;
	}

	@Override
	public String getLocaleLong() {
		return localeLong;
	}

	@Override
	public String getLocaleShort() {
		return localeShort;
	}

	@Override
	public int getLocaleType() {
		return (int) header.languageType;
	}

}
