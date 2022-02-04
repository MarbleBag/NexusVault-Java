package nexusvault.test.bin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import nexusvault.format.bin.LanguageDictionary;
import nexusvault.format.bin.LanguageDictionary.Locale;
import nexusvault.format.bin.LanguageReader;
import nexusvault.format.bin.LanguageWriter;

class CreateWriteAndRead {

	@Test
	public void testCreateEmpty() {
		final var original = new LanguageDictionary(new Locale(0, "en", "eng", "english"), new HashMap<>());
		final var binary = LanguageWriter.toBinary(original);
		final var recreated = LanguageReader.read(binary);
		assertEquals(original, recreated);
	}

	@Test
	public void testCreateNonEmpty() {
		final var original = new LanguageDictionary(new Locale(0, "en", "eng", "english"), new HashMap<>());
		original.entries.put(1L, "Chilly break of day");
		original.entries.put(2L, "An old, gorgeous rabbit roars");
		original.entries.put(3L, "enjoying the cow");
		final var binary = LanguageWriter.toBinary(original);
		final var recreated = LanguageReader.read(binary);
		assertEquals(original, recreated);
	}

}
