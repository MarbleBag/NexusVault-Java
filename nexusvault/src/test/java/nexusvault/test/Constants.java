package nexusvault.test;

import java.io.File;
import java.nio.file.Path;

public final class Constants {
	private Constants() {
	}

	public static final Path PROJECT_DIR = new File("src/test").getAbsoluteFile().toPath();
	public static final Path RESOURCE_DIRECTORY = PROJECT_DIR.resolve("resources");
	public static final Path RESOURCE_OUT_DIRECTORY = RESOURCE_DIRECTORY.resolve("out");

}
