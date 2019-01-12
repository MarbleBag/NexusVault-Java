package nexusvault.format.m3.export.gltf;

import java.nio.file.Path;

public interface GlTFExportMonitor {
	/**
	 * TODO<br>
	 * <b>WIP</b><br>
	 * Up on learning more about m3 materials, this method may change to encompass this
	 */
	void requestTextures(String textureId, ResourceBundle resourceBundle);

	void newFile(Path path);
}