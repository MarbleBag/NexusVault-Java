package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import nexusvault.vault.codec.LzmaCodec;
import nexusvault.vault.codec.ZipCodec;

class Codecs {

	private static final String content = "There once was a old man called ralf.\nHe said, \"See the miniature golf!\"\nIt was rather same,But not very ballgame,He couldn't resist the miniature golf.";

	@Test
	public void testLzma() {
		final var bytes = content.getBytes(StandardCharsets.UTF_8);
		final var encoded = LzmaCodec.encode(bytes);
		final var decoded = LzmaCodec.decode(encoded, bytes.length);
		assertEquals(content, new String(decoded, StandardCharsets.UTF_8));
	}

	@Test
	public void testZip() {
		final var bytes = content.getBytes(StandardCharsets.UTF_8);
		final var encoded = ZipCodec.encode(bytes);
		final var decoded = ZipCodec.decode(encoded, bytes.length);
		assertEquals(content, new String(decoded, StandardCharsets.UTF_8));
	}

}
