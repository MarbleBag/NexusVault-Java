package nexusvault.format.bin;

import java.util.Collection;

public interface LanguageDictionary extends Iterable<LanguageEntry> {

	long entryCount();

	Collection<Integer> getAllTextIds();

	String getText(int id);

	boolean hasText(int id);

	int getLocaleType();

	String getLocaleTag();

	String getLocaleLong();

	String getLocaleShort();

}
