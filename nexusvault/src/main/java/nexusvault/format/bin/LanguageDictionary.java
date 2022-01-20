package nexusvault.format.bin;

import java.util.Map;
import java.util.Objects;

public final class LanguageDictionary {
	public static final class Locale {
		public int type;
		public String tagName;
		public String shortName;
		public String longName;

		public Locale(int type, String tagName, String shortName, String longName) {
			this.type = type;
			this.tagName = Objects.requireNonNull(tagName, "Argument: 'tagName'");
			this.shortName = Objects.requireNonNull(shortName, "Argument: 'shortName'");
			this.longName = Objects.requireNonNull(longName, "Argument: 'longName'");
		}
	}

	public final Locale locale;

	/**
	 * id, text
	 */
	public final Map<Integer, String> entries;

	public LanguageDictionary(Locale locale, Map<Integer, String> entries) {
		this.locale = Objects.requireNonNull(locale, "Argument: 'locale'");
		this.entries = Objects.requireNonNull(entries, "Argument: 'entries'");
	}
}
