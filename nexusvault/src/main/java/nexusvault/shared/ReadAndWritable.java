package nexusvault.shared;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;

public interface ReadAndWritable {
	public void read(BinaryReader reader);

	public void write(BinaryWriter writer);
}
