package nexusvault.format.m3.export.gltf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.GltfModels;
import de.javagl.jgltf.model.io.GltfAsset;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import kreed.io.util.BinaryWriter;
import kreed.io.util.WritableByteChannelBinaryWriter;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.ModelGeometry;
import nexusvault.format.m3.ModelMesh;
import nexusvault.format.m3.ModelVertex;
import nexusvault.shared.exception.IntegerOverflowException;

/**
 * Exports {@link nexusvault.format.m3.Model m3-models} as <tt>gltf</tt> <br>
 *
 * @see #exportModel(Path, String, Model)
 * @see <a href="https://github.com/KhronosGroup/glTF">https://github.com/KhronosGroup/glTF</a>
 */
public class Exporter {

	private static final GlTFComponentType INDEX_COMPONENT_TYPE = GlTFComponentType.UINT32;

	private Model model;
	private String outputFileName;
	private Path outputDirectory;

	private GlTF gltf;

	private VertexField[] fieldAccessors;
	private int vertexSizeInBytes;
	private int minIndex;
	private int maxIndex;

	private Node baseNode;

	/**
	 * Exports the given model to the given directory. The exporter may procude multiple files. <br>
	 * Supported functions:
	 * <ul>
	 * <li>2 UV maps
	 * </ul>
	 * <p>
	 * This process is not thread safe.
	 *
	 * @param directory
	 *            output directory
	 * @param fileName
	 *            name given to the output gltf file, will also be used to name model and may be used to name additional resources produces by the export
	 *            process
	 * @param model
	 *            model to export
	 * @throws IOException
	 */
	public void exportModel(Path directory, String fileName, Model model) throws IOException {
		setOutputDirectory(directory);
		setOutputFileName(fileName);
		setExportModel(model);
		computeVertex();

		prepareOutputDirectory();
		prepareGltf();

		processMeshes();

		writeGltf();
	}

	private void processMeshes() throws IOException {
		addScene();
		prepareBinaryBuffer();

		final ModelGeometry modelGeometry = model.getModelGeometry();
		long bufferViewOffset = 0;
		int meshId = 0;
		for (final ModelMesh modelMesh : modelGeometry.getMeshes()) {
			writeMeshToBuffer(modelMesh);
			bufferViewOffset = addBufferView(modelMesh, meshId, bufferViewOffset);
			addAccessor(modelMesh, meshId);
			addMeshToScene(modelMesh, meshId);
			meshId += 1;
		}
	}

	private void addScene() {
		final Scene scene = new Scene();
		scene.addNodes(0);
		gltf.addScenes(scene);
		gltf.setScene(0);

		baseNode = new Node();
		baseNode.setName(outputFileName);
		gltf.addNodes(baseNode);
	}

	private void writeMeshToBuffer(ModelMesh modelMesh) throws IOException {
		final String binaryName = outputFileName + ".buffer.bin";
		final Path binaryBufferFile = outputDirectory.resolve(binaryName);

		final SeekableByteChannel channel = Files.newByteChannel(binaryBufferFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.APPEND);

		for (final VertexField vertexField : fieldAccessors) {
			vertexField.resetField();
		}

		try (BinaryWriter writer = new WritableByteChannelBinaryWriter(channel, ByteBuffer.allocateDirect(1024 * 1024 * 1).order(ByteOrder.LITTLE_ENDIAN))) {
			for (final ModelVertex vertex : modelMesh.getVertices()) {
				for (final VertexField vertexField : fieldAccessors) {
					vertexField.writeTo(writer, vertex);
				}
			}

			minIndex = Integer.MAX_VALUE;
			maxIndex = 0;

			for (final int index : modelMesh.getIndices()) {
				switch (INDEX_COMPONENT_TYPE) {
					case UINT32:
						writer.writeInt32(index);
						break;
					case UINT16:
						writer.writeInt16(index);
						break;
					default:
						throw new IllegalStateException("TODO");
				}

				minIndex = Math.min(minIndex, index);
				maxIndex = Math.max(maxIndex, index);
			}

			writer.flush();

			final long writtenBytes = writer.getPosition();
			if ((writtenBytes < 0) || (writtenBytes > Integer.MAX_VALUE)) {
				throw new IntegerOverflowException(String.format("jglTF only supports binary buffer within a size of %d bytes", Integer.MAX_VALUE));
			}

			if ((gltf.getBuffers() != null) && !gltf.getBuffers().isEmpty()) {
				final Buffer buffer = gltf.getBuffers().get(0); // only supports one buffer
				buffer.setByteLength(buffer.getByteLength() + (int) writtenBytes);
			} else {
				final Buffer buffer = new Buffer();
				buffer.setByteLength((int) writtenBytes);
				buffer.setUri(binaryName);
				buffer.setName("binary_buffer");
				gltf.addBuffers(buffer);
			}
		}
	}

	private void prepareBinaryBuffer() throws IOException {
		final String binaryName = outputFileName + ".buffer.bin";
		final Path binaryBufferFile = outputDirectory.resolve(binaryName);
		Files.deleteIfExists(binaryBufferFile);
		Files.createFile(binaryBufferFile);
	}

	private void setExportModel(Model model) {
		if (model == null) {
			throw new IllegalArgumentException("'model' must not be null");
		}
		this.model = model;
	}

	private void setOutputFileName(String fileName) {
		if (fileName == null) {
			throw new IllegalArgumentException("'fileName' must not be null");
		}
		this.outputFileName = fileName;
	}

	private void setOutputDirectory(Path directory) {
		if (directory == null) {
			throw new IllegalArgumentException("'directory' must not be null");
		}
		this.outputDirectory = directory;
	}

	private int getOffsetWithinVertex(LinkedList<VertexField> vertexFields) {
		int offset = 0;
		if (!vertexFields.isEmpty()) {
			final VertexField lastField = vertexFields.getLast();
			offset = lastField.getFieldOffset() + lastField.getSizeInBytes();
		}
		return offset;
	}

	private void computeVertex() {
		final ModelGeometry modelGeometry = model.getModelGeometry();

		final LinkedList<VertexField> vertexFields = new LinkedList<>();

		if (modelGeometry.hasVertexLocation()) {
			final int offset = getOffsetWithinVertex(vertexFields);
			final VertexField field = new VertexFieldPosition(offset);
			vertexFields.add(field);
		}

		if (modelGeometry.hasVertex1TextureCoords() || modelGeometry.hasVertex2TextureCoords()) {
			final int offset = getOffsetWithinVertex(vertexFields);
			final VertexField field = new VertexFieldTexCoords1(offset);
			vertexFields.add(field);
		}

		if (modelGeometry.hasVertex2TextureCoords()) {
			final int offset = getOffsetWithinVertex(vertexFields);
			final VertexField field = new VertexFieldTexCoords2(offset);
			vertexFields.add(field);
		}

		if (modelGeometry.hasVertexBoneIndices()) {
			// TODO
		}
		if (modelGeometry.hasVertexBoneWeights()) {
			// TODO
		}

		this.fieldAccessors = vertexFields.toArray(new VertexField[vertexFields.size()]);
		this.vertexSizeInBytes = 0;
		for (final VertexField vertexField : this.fieldAccessors) {
			this.vertexSizeInBytes += vertexField.getSizeInBytes();
		}
	}

	private void addMeshToScene(ModelMesh modelMesh, int meshId) {
		final String name = String.format("_%s_%d", outputFileName, modelMesh.getMeshIndex());

		final Node node = new Node();
		node.setMesh(meshId);
		node.setName("node" + name);
		gltf.addNodes(node);
		baseNode.addChildren(meshId + 1);

		final Mesh mesh = new Mesh();
		gltf.addMeshes(mesh);

		mesh.setName("mesh" + name);
		final List<MeshPrimitive> primitives = new ArrayList<>(1);
		{
			final MeshPrimitive primitive = new MeshPrimitive();
			primitive.setMode(GlTFMode.TRIANGLES.getId());
			final int baseAccessorId = meshId * (fieldAccessors.length + 1);
			for (int i = 0; i < fieldAccessors.length; ++i) {
				final VertexField field = fieldAccessors[i];
				primitive.addAttributes(field.getAttributeKey(), baseAccessorId + i);
			}
			primitive.setIndices(baseAccessorId + fieldAccessors.length);
			primitives.add(primitive);
		}
		mesh.setPrimitives(primitives);
	}

	private void addAccessor(ModelMesh modelMesh, int meshId) {
		addFieldAccessor(modelMesh, meshId);
		addIndexAccessor(modelMesh, meshId);
	}

	private void addFieldAccessor(ModelMesh modelMesh, int meshId) {
		final long vertexCount = modelMesh.getVertexCount();
		if ((vertexCount < 0) || (vertexCount > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException(String.format("jglTF only supports accessor with a element limit of %d", Short.MAX_VALUE));
		}

		final int bufferViewIndex = meshId * 2;

		for (int i = 0; i < fieldAccessors.length; i++) {
			final VertexField vertexField = fieldAccessors[i];

			final Accessor accessor = new Accessor();
			gltf.addAccessors(accessor);
			final int accessorId = (meshId * (fieldAccessors.length + 1)) + i;
			accessor.setName(String.format("accessor_%s_%d", vertexField.getNameShort(), accessorId));

			accessor.setBufferView(bufferViewIndex);
			accessor.setByteOffset(vertexField.getFieldOffset());

			accessor.setComponentType(vertexField.getComponentType()); // float
			accessor.setType(vertexField.getType());
			accessor.setCount((int) vertexCount);

			if (vertexField.hasMinimum()) {
				accessor.setMin(vertexField.getMinimum());
			}

			if (vertexField.hasMaximum()) {
				accessor.setMax(vertexField.getMaximum());
			}
		}
	}

	private void addIndexAccessor(ModelMesh modelMesh, int meshId) {
		final int bufferViewIndex = meshId * 2;

		final Accessor idxAccessor = new Accessor();
		gltf.addAccessors(idxAccessor);

		final long indexCount = modelMesh.getIndexCount();
		if ((indexCount < 0) || (indexCount > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException(String.format("jglTF only supports accessor with a element limit of %d", Integer.MAX_VALUE));
		}

		final int accessorId = (meshId * (fieldAccessors.length + 1)) + fieldAccessors.length;
		idxAccessor.setName(String.format("accessor_idx_%d", accessorId));
		idxAccessor.setBufferView(bufferViewIndex + 1);
		idxAccessor.setByteOffset(0);
		idxAccessor.setCount((int) indexCount);
		idxAccessor.setComponentType(GlTFComponentType.UINT32.getId());
		idxAccessor.setType(GlTFType.SCALAR.getId());
		idxAccessor.setMax(new Number[] { maxIndex });
		idxAccessor.setMin(new Number[] { minIndex });
	}

	private long addBufferView(ModelMesh modelMesh, int meshId, long bufferViewOffset) {
		bufferViewOffset = addBufferViewGeometry(modelMesh, meshId, bufferViewOffset);
		bufferViewOffset = addBufferViewIndices(modelMesh, meshId, bufferViewOffset);
		return bufferViewOffset;
	}

	private long addBufferViewGeometry(ModelMesh modelMesh, int meshId, long bufferViewOffset) {
		final long vertexByteCount = modelMesh.getVertexCount() * vertexSizeInBytes;

		if ((bufferViewOffset < 0) || (bufferViewOffset > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException(String.format("jglTF only supports binary buffer within a size of %d bytes", Integer.MAX_VALUE));
		}

		if ((vertexByteCount < 0) || (vertexByteCount > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException(String.format("jglTF only supports bufferviews within a size of %d bytes", Integer.MAX_VALUE));
		}

		final BufferView geometryBufferView = new BufferView();
		gltf.addBufferViews(geometryBufferView);

		geometryBufferView.setBuffer(0);
		geometryBufferView.setByteOffset((int) bufferViewOffset);
		geometryBufferView.setByteLength((int) vertexByteCount);
		geometryBufferView.setByteStride(vertexSizeInBytes);
		geometryBufferView.setName(String.format("bufferview_geo_%d", meshId));

		bufferViewOffset += vertexByteCount;
		return bufferViewOffset;
	}

	private long addBufferViewIndices(ModelMesh modelMesh, int meshId, long bufferViewOffset) {
		final long indexByteCount = modelMesh.getIndexCount() * INDEX_COMPONENT_TYPE.getByteCount();

		if ((bufferViewOffset < 0) || (bufferViewOffset > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException(String.format("jglTF only supports binary buffer within a size of %d bytes", Integer.MAX_VALUE));
		}

		if ((indexByteCount < 0) || (indexByteCount > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException(String.format("jglTF only supports bufferviews within a size of %d bytes", Integer.MAX_VALUE));
		}

		final BufferView indexBufferView = new BufferView();
		gltf.addBufferViews(indexBufferView);

		indexBufferView.setBuffer(0);
		indexBufferView.setByteOffset((int) bufferViewOffset);
		indexBufferView.setByteLength((int) indexByteCount);
		indexBufferView.setName(String.format("bufferview_idx_%d", meshId));

		bufferViewOffset += indexByteCount;
		return bufferViewOffset;
	}

	private void prepareOutputDirectory() throws IOException {
		Files.createDirectories(outputDirectory);
	}

	private void prepareGltf() {
		this.gltf = new GlTF();

		final Asset asset = new Asset();
		asset.setVersion("2.0");
		asset.setGenerator("nexusvault-gltf-exporter");
		gltf.setAsset(asset);
	}

	private void writeGltf() throws IOException {
		final GltfAsset gltfAsset = new GltfAssetV2(gltf, null);
		final GltfModel gltfModel = GltfModels.create(gltfAsset);
		final GltfModelWriter gltfModelWriter = new GltfModelWriter();
		final Path outputFile = outputDirectory.resolve(outputFileName + ".gltf");
		gltfModelWriter.write(gltfModel, outputFile.toFile());
	}
}
