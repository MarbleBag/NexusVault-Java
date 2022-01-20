package nexusvault.export.m3.obj;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

import nexusvault.format.m3.Model;

public class ObjExporter {

	static interface ResourceStorage {

	}

	static interface Resource {

	}

	static interface ResourceRequest {
		Resource requestResource(ResourceStorage store, String resourceName);
	}

	public void exportModel(Path directory, String fileName, Model model) throws IOException {
		if (directory == null) {
			throw new IllegalArgumentException("'directory' must not be null.");
		}
		if (model == null) {
			throw new IllegalArgumentException("'model' must not be null.");
		}
		if (fileName == null) {
			throw new IllegalArgumentException("'fileName' must not be null.");
		}
		if (fileName.length() == 0) {
			throw new IllegalArgumentException("'fileName' must not be empty.");
		}

		Files.createDirectories(directory);

		writeObj(directory, fileName, model);
		writeMtl(directory, fileName, model);
	}

	private void writeObj(Path directory, String fileName, Model model) throws IOException {
		final var file = directory.resolve(fileName + ".obj");
		try (var writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			writeObj(model, writer, fileName);
		}
	}

	private void writeMtl(Path directory, String fileName, Model model) throws IOException {
		final var file = directory.resolve(fileName + ".mtl");
		try (var writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			writeMtl(model, writer, fileName);
		}
	}

	private void writeObj(Model model, BufferedWriter writer, String modelName) throws IOException {
		writer.append("mtlib ").append(modelName).append(".mtl\n");
		writer.append("o ").append(modelName).append('\n');

		final var modelGeometry = model.getGeometry();
		final var meshNamePattern = modelName + ".%0" + String.valueOf(modelGeometry.getMeshCount()).length() + "d";

		int startIndex = 0;
		for (final var modelMesh : modelGeometry.getMeshes()) {
			writer.append("g ").append(String.format(meshNamePattern, modelMesh.getMeshIndex())).append('\n');

			final var vertices = modelMesh.getVertices();

			if (modelGeometry.hasVertexLocation()) {
				for (final var vertex : vertices) {
					final float x = vertex.getLocationX();
					final float y = vertex.getLocationY();
					final float z = vertex.getLocationZ();
					writer.append(String.format(Locale.US, "v %f %f %f", x, y, z)).append('\n');
				}
			}

			if (modelGeometry.hasVertex1TextureCoords()) {
				for (final var vertex : vertices) {
					final float u = vertex.getTextureCoordU1();
					final float v = vertex.getTextureCoordV1();
					writer.append(String.format(Locale.US, "vt %f %f", u, v)).append('\n');
				}
			}

			if (modelGeometry.hasVertex2TextureCoords()) {
				for (final var vertex : vertices) {
					final float u = vertex.getTextureCoordU2();
					final float v = vertex.getTextureCoordV2();
					writer.append(String.format(Locale.US, "vt %f %f", u, v)).append('\n');
				}
			}

			// TODO set texture

			final int[] indices = modelMesh.getIndices();
			for (int i = 0; i < indices.length; i += 3) {
				final int indexA = indices[i + 0] + 1 + startIndex;
				final int indexB = indices[i + 1] + 1 + startIndex;
				final int indexC = indices[i + 2] + 1 + startIndex;
				// writer.append(String.format(Locale.US, "f %1$d/%1$d/%1$d %2$d/%2$d/%2$d %3$d/%3$d/%3$d", indexA, indexB, indexC));
				writer.append(String.format(Locale.US, "f %1$d/%1$d %2$d/%2$d %3$d/%3$d", indexA, indexB, indexC)); // no normals
				writer.append('\n');
			}
			startIndex += modelMesh.getVertexCount();
		}
	}

	private void writeMtl(Model model, BufferedWriter writer, String fileName) {
		// for (int i = 0; i < textureCount; ++i) {
		// final String textureName = null;
		// final Resource resource = requestResource(textureName);
		// // TODO
		// writer.append("newmtl textureMaterial_").append(String.valueOf(i)).append('\n');
		//// writer.append("Ka 1.000 1.000 1.000").append('\n'); // rgb ambient reflectivity
		//// writer.append("Kd 1.000 1.000 1.000").append('\n'); // rgb diffuse reflectivity
		//// writer.append("Ks 0.000 0.000 0.000").append('\n'); // rgb specular reflectivity
		// writer.append("illum ").append(String.valueOf(ObjIllum.ZERO.getValue())).append('\n');
		// writer.append("map_Ka").append(textureName).append('\n');
		// writer.append("map_Kd").append(textureName).append('\n');
		// writer.append("map_Bump").append(textureName).append('\n'); //doesn't support normals

		// writer.append('\n');
		// }
	}

}
