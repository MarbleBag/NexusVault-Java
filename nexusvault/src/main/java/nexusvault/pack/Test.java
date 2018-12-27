package nexusvault.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import kreed.io.util.SeekableByteChannelBinaryReader;
import nexusvault.pack.index.IndexFileReader;

class Test {

	final public static Path WILDSTAR_PATCH_FOLDER = Paths.get("F:\\Wildstar Sniffing\\WildStar\\Patch");
	final public static Path WILDSTAR_INDEX_FILE = WILDSTAR_PATCH_FOLDER.resolve("ClientData.index");

	public static void main(String[] args) throws IOException {

		final IndexFileReader reader = new IndexFileReader();
		try (SeekableByteChannel stream = Files.newByteChannel(WILDSTAR_INDEX_FILE, EnumSet.of(StandardOpenOption.READ))) {
			final ByteBuffer buffer = ByteBuffer.allocateDirect(32 * 1024).order(ByteOrder.LITTLE_ENDIAN);
			reader.read(new SeekableByteChannelBinaryReader(stream, buffer));
		}

		// TODO Auto-generated method stub

	}

}
