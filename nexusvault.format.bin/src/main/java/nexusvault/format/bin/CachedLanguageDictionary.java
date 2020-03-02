package nexusvault.format.bin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

final class CachedLanguageDictionary implements LanguageDictionary {

	private final LanguageDictionary dictionary;
	private final Map<Integer, String> cache;

	public CachedLanguageDictionary(LanguageDictionary dictionary) {
		this.dictionary = Objects.requireNonNull(dictionary);
		cache = new HashMap<>();
	}

	@Override
	public Iterator<LanguageEntry> iterator() {
		return new Iterator<>() {

			private final Iterator<LanguageEntry> itr = dictionary.iterator();

			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public LanguageEntry next() {
				final LanguageEntry entry = itr.next();
				return new LanguageEntry() {
					@Override
					public String getText() {
						final Integer key = Integer.valueOf(entry.getId());
						if (cache.containsKey(key)) {
							return cache.get(key);
						}

						final String txt = entry.getText();
						if (txt != null) {
							cache.put(key, txt);
						}
						return txt;
					}

					@Override
					public int getId() {
						return entry.getId();
					}
				};
			}
		};
	}

	@Override
	public long entryCount() {
		return dictionary.entryCount();
	}

	@Override
	public Collection<Integer> getAllTextIds() {
		return dictionary.getAllTextIds();
	}

	@Override
	public String getText(int id) {
		final Integer key = Integer.valueOf(id);
		if (cache.containsKey(key)) {
			return cache.get(key);
		} else {
			final String txt = dictionary.getText(key);
			if (txt != null) {
				cache.put(key, txt);
			}
			return txt;
		}
	}

	@Override
	public boolean hasText(int id) {
		return dictionary.hasText(id);
	}

	@Override
	public int getLocaleType() {
		return dictionary.getLocaleType();
	}

	@Override
	public String getLocaleTag() {
		return dictionary.getLocaleTag();
	}

	@Override
	public String getLocaleLong() {
		return dictionary.getLocaleLong();
	}

	@Override
	public String getLocaleShort() {
		return dictionary.getLocaleShort();
	}

}
