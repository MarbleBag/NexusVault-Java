package nexusvault.format.bin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import nexusvault.format.bin.LanguageReader.LanguageEntry;

final class PreloadedLanguageDictionary implements LanguageDictionary {

	private final int localeType;
	private final String localeTag;
	private final String localeLong;
	private final String localeShort;
	private final Map<Integer, LanguageEntry> map;

	public PreloadedLanguageDictionary(int localeType, String localeTag, String localeLong, String localeShort, LanguageEntry[] entries) {
		this.localeType = localeType;
		this.localeTag = localeTag;
		this.localeLong = localeLong;
		this.localeShort = localeShort;
		this.map = Collections.unmodifiableMap(Arrays.stream(entries).collect(Collectors.toMap(e -> e.getId(), e -> e)));
	}

	@Override
	public Iterator<LanguageEntry> iterator() {
		final Collection<LanguageEntry> o = map.values();
		return o.iterator();
	}

	@Override
	public long entryCount() {
		return map.size();
	}

	@Override
	public Collection<Integer> getAllTextIds() {
		return map.keySet();
	}

	@Override
	public String getText(int id) {
		final LanguageEntry e = map.get(Integer.valueOf(id));
		return e == null ? null : e.getText();
	}

	@Override
	public boolean hasText(int id) {
		return map.containsKey(Integer.valueOf(id));
	}

	@Override
	public int getLocaleType() {
		return localeType;
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

}
