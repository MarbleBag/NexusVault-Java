package nexusvault.test;

import java.io.File;
import java.nio.file.Path;

public final class Constants {
	private Constants() {
	}

	public static final Path PROJECT_DIR = new File("src/test").toPath();
	public static final Path RESOURCE_DIRECTORY = new File("src/test/resources").toPath();

}
