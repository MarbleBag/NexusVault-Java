package nexusvault.format.m3.export.gltf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Image;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MaterialNormalTextureInfo;
import de.javagl.jgltf.impl.v2.MaterialOcclusionTextureInfo;
import de.javagl.jgltf.impl.v2.MaterialPbrMetallicRoughness;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.impl.v2.Skin;
import de.javagl.jgltf.impl.v2.Texture;
import de.javagl.jgltf.impl.v2.TextureInfo;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.GltfModels;
import de.javagl.jgltf.model.io.GltfAsset;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import kreed.io.util.BinaryWriter;
import kreed.io.util.WritableByteChannelBinaryWriter;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.ModelBone;
import nexusvault.format.m3.ModelGeometry;
import nexusvault.format.m3.ModelMaterial;
import nexusvault.format.m3.ModelMaterialDescription;
import nexusvault.format.m3.ModelMesh;
import nexusvault.format.m3.ModelTexture;
import nexusvault.format.m3.ModelVertex;
import nexusvault.format.m3.export.gltf.Bi2NiLookUp.Bi2NiEntry;
import nexusvault.shared.exception.IntegerOverflowException;

/**
 * Exports {@link nexusvault.format.m3.Model m3-models} as <tt>gltf</tt> <br>
 * Set up a {@link GlTFExportMonitor} to provide the exported model with textures and to monitor each file this exporter creates.
 *
 * @see #exportModel(Path, String, Model)
 * @see <a href="https://github.com/KhronosGroup/glTF">https://github.com/KhronosGroup/glTF</a>
 */
// TODO add kind of property to control some export functions
public final class GlTFExporter {

	private static final class LookupIndex {
		public int index;
		public int length;

		public LookupIndex(int index, int length) {
			super();
			this.index = index;
			this.length = length;
		}

		public int getIndex() {
			return index;
		}

		public int getLength() {
			return length;
		}

		public int getLastIndex() {
			return index + length;
		}

	}

	private static final GlTFComponentType INDEX_COMPONENT_TYPE = GlTFComponentType.UINT32;

	// TODO
	public GlTFExportMonitor monitor;

	private Model model;
	private String outputFileName;
	private Path outputDirectory;

	private GlTF gltf;

	private List<LookupIndex> materialLookUp;
	private Map<String, int[]> textureLookUpByURI;
	private List<TextureResource> textureResources;
	private Bi2NiLookUp boneIndexToNodeIndexLookUp;

	private VertexField[] fieldAccessors;
	private int vertexSizeInBytes;
	private int minIndex;
	private int maxIndex;

	private Node baseNode;

	private Path binaryBufferFile;

	public GlTFExporter() {

	}

	public void setGlTFExportMonitor(GlTFExportMonitor monitor) {
		this.monitor = monitor;
	}

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
		try {
			setOutputDirectory(directory);
			setOutputFileName(fileName);
			setExportModel(model);
			computeVertex();

			prepareOutputDirectory();
			prepareGltf();
			prepareLookups();

			prepareDefaultScene();
			prepareBinaryBuffer();

			processMeshes();

			writeGltf();
		} finally {
			clearLookups();
			clearExportModel();
			clearGltf();
		}
	}

	private int getLastUsedIndex(List<LookupIndex> lookups) {
		if (lookups.isEmpty()) {
			return 0;
		}
		final LookupIndex last = lookups.get(lookups.size() - 1);
		return last.index + last.length;
	}

	private void processMeshes() throws IOException {
		final ModelGeometry modelGeometry = model.getGeometry();
		long bufferViewOffset = 0;
		int meshId = 0;

		addTextures();
		addMaterials();

		for (final ModelMesh modelMesh : modelGeometry.getMeshes()) {
			writeMeshToBuffer(modelMesh);
			bufferViewOffset = addBufferView(modelMesh, meshId, bufferViewOffset);
			addAccessor(modelMesh, meshId);
			addMeshToScene(modelMesh, meshId);

			meshId += 1;
		}

		addSkeleton();
	}

	private void addSkeleton() throws IOException {
		// TODO bone translation, rotation and scaling is missing
		// TODO blender imports gltf with each mesh connected to each bone in order, check if error may be in how the skeleton is exported

		final List<ModelBone> modelBones = model.getBones();

		final SeekableByteChannel channel = Files.newByteChannel(binaryBufferFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.APPEND);
		try (BinaryWriter writer = new WritableByteChannelBinaryWriter(channel, ByteBuffer.allocateDirect(1024 * 1024 * 1).order(ByteOrder.LITTLE_ENDIAN))) {
			final long byteOffset = writer.getPosition();

			for (int i = 0; i < modelBones.size(); ++i) {
				// column major
				writer.writeFloat32(1f);
				writer.writeFloat32(0f);
				writer.writeFloat32(0f);
				writer.writeFloat32(0f);

				writer.writeFloat32(0f);
				writer.writeFloat32(1f);
				writer.writeFloat32(0f);
				writer.writeFloat32(0f);

				writer.writeFloat32(0f);
				writer.writeFloat32(0f);
				writer.writeFloat32(1f);
				writer.writeFloat32(0f);

				writer.writeFloat32(0f);
				writer.writeFloat32(0f);
				writer.writeFloat32(0f);
				writer.writeFloat32(1f);
			}

			final long byteWritten = modelBones.size() * 16;

			final BufferView bufferView = new BufferView();
			bufferView.setName("ViewSkinInverseBindMatrix");
			bufferView.setBuffer(0);
			bufferView.setByteOffset((int) byteOffset);
			bufferView.setByteLength((int) byteWritten);
			gltf.addBufferViews(bufferView);

			final Accessor accessor = new Accessor();
			accessor.setName("AccessorSkinInverseBindMatrix");
			accessor.setBufferView(gltf.getBufferViews().size() - 1);
			accessor.setType(GlTFType.MAT4.getId());
			accessor.setComponentType(GlTFComponentType.FLOAT.getId());
			accessor.setCount(modelBones.size());
			gltf.addAccessors(accessor);
		}

		final Skin skin = new Skin();
		skin.setName("Armature");
		skin.setSkeleton(0);

		// TODO: optional my ass. Bug in GltfModelV2#initSkinModels
		skin.setInverseBindMatrices(gltf.getAccessors().size() - 1); // seems not to work in blender?
		gltf.addSkins(skin);

		for (final ModelBone modelBone : modelBones) {
			final Node node = new Node();
			gltf.addNodes(node);
			node.setName("Bone_" + modelBone.getBoneIndex());

			final int nodeIndex = gltf.getNodes().size() - 1;

			final Bi2NiEntry lookUpEntry = new Bi2NiEntry(modelBone.getBoneIndex(), nodeIndex);
			boneIndexToNodeIndexLookUp.add(lookUpEntry);

			skin.addJoints(nodeIndex);

			if (modelBone.hasParentBone()) {
				lookUpEntry.setParentOriginalIndex(modelBone.getParentBoneReference());
			}
		}

		for (final Bi2NiEntry node : boneIndexToNodeIndexLookUp) {
			// set gltf parents
			if (node.hasParent()) {
				final Bi2NiEntry parent = boneIndexToNodeIndexLookUp.getForOriginalIndex(node.getParentOriginalIndex());
				final Node parentNode = gltf.getNodes().get(parent.getLookUpIndex());
				parentNode.addChildren(node.getLookUpIndex());
			} else {
				baseNode.addChildren(node.getLookUpIndex());
			}

			// update bone translation
			final Node gltfNode = gltf.getNodes().get(node.getLookUpIndex());
			final ModelBone bone = modelBones.get(node.getOriginalIndex());
			final float[] translation = new float[] { bone.getLocationX(), bone.getLocationY(), bone.getLocationZ() };

			if (node.hasParent()) {
				final ModelBone parentBone = modelBones.get(node.getParentOriginalIndex());
				translation[0] -= parentBone.getLocationX();
				translation[1] -= parentBone.getLocationY();
				translation[2] -= parentBone.getLocationZ();
			}

			gltfNode.setTranslation(translation);
		}

	}

	private void prepareLookups() {
		materialLookUp = new ArrayList<>(10);
		textureLookUpByURI = new HashMap<>();
		textureResources = new ArrayList<>(20);
		boneIndexToNodeIndexLookUp = new Bi2NiLookUp();
	}

	private void clearLookups() {
		materialLookUp = null;
		textureLookUpByURI = null;
		textureResources = null;
		boneIndexToNodeIndexLookUp = null;
	}

	private void clearExportModel() {
		this.model = null;
	}

	private void clearGltf() {
		this.gltf = null;
	}

	private void addTextures() throws IOException {
		// TODO what a mess
		final String textureDirName = outputFileName + "_textures";
		final Path textureDir = outputDirectory.resolve(textureDirName);
		Files.createDirectories(textureDir);

		int counter = 0;
		for (final ModelTexture textures : model.getTextures()) {
			final String texturePath = textures.getTexturePath();
			final ResourceBundle bundle = requestTexture(texturePath);

			String imageName = texturePath;
			if (imageName.lastIndexOf('\\') >= 0) {
				imageName = imageName.substring(imageName.lastIndexOf('\\') + 1);
			}

			for (final TextureResource texResource : bundle.getTextureResources()) {
				// TODO any added file needs to be reported
				final Image image = texResource.writeImageTo(textureDir, outputFileName);
				if (image.getBufferView() == null) {
					// this is has to be done because of a bug in de.javagl.jgltf.model.io.GltfModelWriter.GltfModelWriter(), which does not accept an image
					// without a bufferview, even while it's not even using one.
					image.setBufferView(0);
					image.setName(imageName);
				}
				gltf.addImages(image);

				final Texture texture = new Texture();
				texture.setSource(counter++);
				gltf.addTextures(texture);
			}

		}
	}

	private void addMaterials() {
		final List<ModelTexture> textures = model.getTextures();
		for (final ModelMaterial modelMaterial : model.getMaterials()) {
			final List<ModelMaterialDescription> modelMaterialDescriptions = modelMaterial.getMaterialDescriptions();
			materialLookUp.add(new LookupIndex(getLastUsedIndex(materialLookUp), modelMaterialDescriptions.size()));

			for (final ModelMaterialDescription modelMaterialDescription : modelMaterialDescriptions) {
				final Material material = new Material();

				if (textures.size() > modelMaterialDescription.getTextureReferenceA()) {
					final ModelTexture referenceTexture = textures.get(modelMaterialDescription.getTextureReferenceA());
					final int[] textureIndices = requestTextureIndices(referenceTexture.getTexturePath());

					if (textureIndices.length != 0) {
						final MaterialPbrMetallicRoughness materialPbrMetallicRoughness = new MaterialPbrMetallicRoughness();
						{ // diffuse
							final TextureInfo textureInfo = new TextureInfo();
							textureInfo.setIndex(textureIndices[0]);
							materialPbrMetallicRoughness.setBaseColorTexture(textureInfo);
						}

						if (textureIndices.length >= 2) { // Roughness
							final TextureInfo textureInfo = new TextureInfo();
							textureInfo.setIndex(textureIndices[1]);
							materialPbrMetallicRoughness.setMetallicRoughnessTexture(textureInfo);
						}

						material.setPbrMetallicRoughness(materialPbrMetallicRoughness);
					}
				}

				if (textures.size() > modelMaterialDescription.getTextureReferenceB()) {
					final ModelTexture referenceTexture = textures.get(modelMaterialDescription.getTextureReferenceB());
					final int[] textureIndices = requestTextureIndices(referenceTexture.getTexturePath());
					if (textureIndices.length != 0) {
						{ // normal
							final MaterialNormalTextureInfo normal = new MaterialNormalTextureInfo();
							normal.setIndex(textureIndices[0]);
							material.setNormalTexture(normal);
						}

						{ // occlusion
							final MaterialOcclusionTextureInfo occlusion = new MaterialOcclusionTextureInfo();
							occlusion.setIndex(textureIndices[1]);
							material.setOcclusionTexture(occlusion);
						}

						{ // emissive
							final TextureInfo textureInfo = new TextureInfo();
							textureInfo.setIndex(textureIndices[2]);
							material.setEmissiveTexture(textureInfo);
							material.setEmissiveFactor(new float[] { 1f, 1f, 1f });
						}
					}
				}
				gltf.addMaterials(material);
			}
		}
	}

	private int[] requestTextureIndices(String textureId) {
		return textureLookUpByURI.getOrDefault(textureId, new int[0]);
	}

	private ResourceBundle requestTexture(String textureId) {
		if (textureLookUpByURI.containsKey(textureId)) {
			final int[] indices = textureLookUpByURI.get(textureId);
			final ResourceBundle resourceBundle = new ResourceBundle();
			for (final int idx : indices) {
				resourceBundle.addTextureResource(textureResources.get(idx));
			}
			return resourceBundle;
		}

		final ResourceBundle resourceBundle = new ResourceBundle();

		if ((textureId == null) || textureId.isEmpty()) {

		} else if (monitor != null) {
			try {
				monitor.requestTextures(textureId, resourceBundle); // TODO the way this works is not ideal yet
			} catch (final Exception e) {
				throw new OnTextureRequestException(e);
			}
		} else {

		}

		final List<TextureResource> textureResources = resourceBundle.getTextureResources();
		int startIndex = this.textureResources.size();
		final int[] indices = new int[textureResources.size()];
		int idx = 0;
		for (final TextureResource res : textureResources) {
			indices[idx++] = startIndex++;
			this.textureResources.add(res);
		}
		textureLookUpByURI.put(textureId, indices);

		return resourceBundle;
	}

	private void prepareDefaultScene() {
		final Scene scene = new Scene();
		scene.addNodes(0);
		gltf.addScenes(scene);
		gltf.setScene(0);

		baseNode = new Node();
		baseNode.setName(outputFileName);
		gltf.addNodes(baseNode);
	}

	private void writeMeshToBuffer(ModelMesh modelMesh) throws IOException {

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
				buffer.setUri(binaryBufferFile.getFileName().toString());
				buffer.setName("binary_buffer");
				gltf.addBuffers(buffer);
			}
		}
	}

	private void prepareBinaryBuffer() throws IOException {
		final String binaryName = outputFileName + ".buffer.bin";
		binaryBufferFile = outputDirectory.resolve(binaryName);

		Files.deleteIfExists(binaryBufferFile);
		Files.createFile(binaryBufferFile);

		if (monitor != null) {
			try {
				monitor.newFile(binaryBufferFile);
			} catch (final Throwable t) {
				throw new OnAddFileException(t);
			}
		}
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
		final ModelGeometry modelGeometry = model.getGeometry();

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
			final int offset = getOffsetWithinVertex(vertexFields);
			final VertexField field = new VertexFieldBoneIndices(offset, model.getBoneLookUp());
			vertexFields.add(field);
		}

		if (modelGeometry.hasVertexBoneWeights()) {
			final int offset = getOffsetWithinVertex(vertexFields);
			final VertexField field = new VertexFieldBoneWeights(offset);
			vertexFields.add(field);
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
		node.setSkin(0);
		gltf.addNodes(node);
		baseNode.addChildren(gltf.getNodes().size() - 1);

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
			primitive.setMaterial(materialLookUp.get(modelMesh.getMaterialReference()).index);
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
