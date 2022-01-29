package nexusvault.export.bin.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;

import nexusvault.format.bin.LanguageDictionary;
import nexusvault.shared.exception.ImportException;

/**
 * Simple exporter which is able to convert a {@link LanguageDictionary} to csv. The default element delimiter is <code>;</code> but can be changed. Each line
 * will end with a <code>line feed</code>.
 */
public final class Csv {

	private final String elementDelimiter;

	public Csv() {
		this(";");
	}

	public Csv(String elementDelimiter) {
		this.elementDelimiter = elementDelimiter;
	}

	public void write(LanguageDictionary dictionary, Writer out) throws IOException {
		out.append(Integer.toString(dictionary.locale.type)).append(this.elementDelimiter);
		out.append(dictionary.locale.tagName).append(this.elementDelimiter);
		out.append(dictionary.locale.shortName).append(this.elementDelimiter);
		out.append(dictionary.locale.longName).append("\n");
		out.append("Code").append(this.elementDelimiter).append("Text").append("\n");
		for (final var entry : dictionary.entries.entrySet()) {
			out.append(entry.getKey().toString());
			out.append(this.elementDelimiter);
			out.append(entry.getValue());
			out.append("\n");
		}
	}

	public LanguageDictionary read(Reader in) throws IOException {
		final var reader = new BufferedReader(in);

		final var localeSplit = reader.readLine().split(this.elementDelimiter, -1);
		if (localeSplit.length != 4) {
			throw new ImportException(); // TODO
		}

		final var locale = new LanguageDictionary.Locale(Integer.parseInt(localeSplit[0]), localeSplit[1], localeSplit[2], localeSplit[3]);

		final var entries = new HashMap<Long, String>();

		String line = null;
		while ((line = reader.readLine()) != null) {
			final var entry = line.split(this.elementDelimiter);
			entries.put(Long.parseLong(line), entry[1]);
		}

		return new LanguageDictionary(locale, entries);
	}

}
