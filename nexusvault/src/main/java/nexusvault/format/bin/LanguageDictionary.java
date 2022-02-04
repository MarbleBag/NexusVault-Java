/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.bin;

import java.util.Map;
import java.util.Objects;

public final class LanguageDictionary {

	public static LanguageDictionary read(byte[] data) {
		return LanguageReader.read(data);
	}

	public static byte[] write(LanguageDictionary dictionary) {
		return LanguageWriter.toBinary(dictionary);
	}

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

		@Override
		public int hashCode() {
			return Objects.hash(this.longName, this.shortName, this.tagName, this.type);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Locale other = (Locale) obj;
			return Objects.equals(this.longName, other.longName) && Objects.equals(this.shortName, other.shortName)
					&& Objects.equals(this.tagName, other.tagName) && this.type == other.type;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("Locale [type=");
			builder.append(this.type);
			builder.append(", tagName=");
			builder.append(this.tagName);
			builder.append(", shortName=");
			builder.append(this.shortName);
			builder.append(", longName=");
			builder.append(this.longName);
			builder.append("]");
			return builder.toString();
		}
	}

	public final Locale locale;

	/**
	 * id, text
	 */
	public final Map<Long, String> entries;

	public LanguageDictionary(Locale locale, Map<Long, String> entries) {
		this.locale = Objects.requireNonNull(locale, "Argument: 'locale'");
		this.entries = Objects.requireNonNull(entries, "Argument: 'entries'");
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.entries, this.locale);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LanguageDictionary other = (LanguageDictionary) obj;
		return Objects.equals(this.entries, other.entries) && Objects.equals(this.locale, other.locale);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("LanguageDictionary [locale=");
		builder.append(this.locale);
		builder.append(", #entries=");
		builder.append(this.entries.size());
		builder.append("]");
		return builder.toString();
	}
}
