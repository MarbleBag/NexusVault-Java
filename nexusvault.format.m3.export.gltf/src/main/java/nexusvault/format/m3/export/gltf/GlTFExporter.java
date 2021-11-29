package nexusvault.format.m3.export.gltf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MaterialNormalTextureInfo;
import de.javagl.jgltf.impl.v2.MaterialOcclusionTextureInfo;
import de.javagl.jgltf.impl.v2.MaterialPbrMetallicRoughness;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Skin;
import de.javagl.jgltf.impl.v2.TextureInfo;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.GltfModels;
import de.javagl.jgltf.model.io.GltfAsset;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import kreed.io.util.BinaryWriter;
import kreed.io.util.WritableByteChannelBinaryWriter;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.ModelMesh;
import nexusvault.format.m3.export.gltf.internal.GlTFComponentType;
import nexusvault.format.m3.export.gltf.internal.GlTFMode;
import nexusvault.format.m3.export.gltf.internal.GlTFType;
import nexusvault.format.m3.export.gltf.internal.GltfHelper;
import nexusvault.format.m3.export.gltf.internal.TextureManager;
import nexusvault.format.m3.export.gltf.internal.vertex.MeshWriter;
import nexusvault.shared.exception.IntegerOverflowException;

public final class GlTFExporter {

	public static GlTFExporter makeExporter() {
		return new GlTFExporter();
	}

	private String modelName;

	private Path outputDirectory;
	private Path binaryBufferFile;

	private Model model;
	private GlTF gltfModel;

	// helper
	private TextureManager textureManager;

	// config
	private boolean isExportMesh = true;
	private boolean isExportBones = true;
	private boolean isExportTextures = true;

	private GlTFExportMonitor monitor;

	private GlTFExporter() {

	}

	private void initialize(Path directory, String modelName, Model model) {
		if (directory == null) {
			throw new IllegalArgumentException("'directory' must not be null");
		}
		this.outputDirectory = directory;

		if (modelName == null || modelName.isEmpty()) {
			throw new IllegalArgumentException("'modelName' must not be null or empty");
		}
		this.modelName = modelName;

		if (model == null) {
			throw new IllegalArgumentException("'model' must not be null");
		}
		this.model = model;

		this.gltfModel = GltfHelper.createBaseModel();
		GltfHelper.getRootNode(this.gltfModel).setName(modelName);

	}

	private void dispose() {
		this.outputDirectory = null;
		this.binaryBufferFile = null;
		this.modelName = null;

		this.model = null;
		this.gltfModel = null;

		this.textureManager = null;
	}

	public void setGlTFExportMonitor(GlTFExportMonitor monitor) {
		this.monitor = monitor;
	}

	public boolean isExportMesh() {
		return this.isExportMesh;
	}

	public void setExportMesh(boolean value) {
		this.isExportMesh = value;
	}

	public boolean isExportSkeleton() {
		return this.isExportBones;
	}

	public void setExportBones(boolean value) {
		this.isExportBones = value;
	}

	public boolean isExportTextures() {
		return this.isExportTextures;
	}

	public void setExportTextures(boolean value) {
		this.isExportTextures = value;
	}

	public void exportModel(Path directory, String modelName, Model model) throws IOException {
		try {
			initialize(directory, modelName, model);

			if (isExportMesh()) {
				writeMeshData();
			}

			if (isExportSkeleton()) {
				writeSkeletonData();
			}

			if (isExportTextures()) {
				writeTextureData();
				writeMaterialData();
			}

			writeGltf();
		} finally {
			dispose();
		}
	}

	private void writeSkeletonData() throws IOException { // TODO
		final var bones = this.model.getBones();
		if (bones.isEmpty()) {
			return; // done
		}

		final var writeResult = writeToBinary((writer, position) -> {
			for (int i = 0; i < bones.size(); ++i) {
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

				writer.writeFloat32(-bones.get(i).getLocationX());
				writer.writeFloat32(-bones.get(i).getLocationY());
				writer.writeFloat32(-bones.get(i).getLocationZ());
				writer.writeFloat32(1f);

				// final var bone = bones.get(i);
				// final var transMatrix = bone.getTransformationMatrix();
				// final var invTransMatrix = MathUtil.inverse(transMatrix);
				// for (final var r : invTransMatrix) {
				// writer.writeFloat32(r);
				// }
			}
		});

		final var bufferViewIdx = GltfHelper.addBufferView(this.gltfModel, writeResult.writeStart, writeResult.writeLength);
		final var bufferView = this.gltfModel.getBufferViews().get(bufferViewIdx);
		bufferView.setName("ViewSkinInverseBindMatrix");

		final var accessorIdx = GltfHelper.addAccessor(this.gltfModel, GlTFType.MAT4, GlTFComponentType.FLOAT);
		final var accessor = this.gltfModel.getAccessors().get(accessorIdx);
		accessor.setName("AccessorSkinInverseBindMatrix");
		accessor.setBufferView(bufferViewIdx);
		accessor.setCount(bones.size());

		final var skin = new Skin();
		skin.setName("Armature");
		// skin.setSkeleton(0);
		skin.setSkeleton(GltfHelper.getNodeCount(this.gltfModel)); // first node we will add is also skeleton root

		// TODO: By spec this should be optional. Bug in GltfModelV2#initSkinModels
		skin.setInverseBindMatrices(accessorIdx);
		this.gltfModel.addSkins(skin);

		final var bone2Node = new HashMap<Integer, Integer>(); // bone index => node index
		final var nodeNameFormat = "Joint_%0" + String.valueOf(bones.size()).length() + "d";
		for (final var bone : bones) {
			final var nodeIdx = GltfHelper.getNodeCount(this.gltfModel);
			final var node = new Node();

			node.setName(String.format(nodeNameFormat, bone.getBoneIndex()));
			bone2Node.put(bone.getBoneIndex(), nodeIdx);

			skin.addJoints(nodeIdx);

			if (skin.getSkeleton() == null) {
				skin.setSkeleton(nodeIdx);
			}

			this.gltfModel.addNodes(node);
		}

		final var rootNode = GltfHelper.getRootNode(this.gltfModel);
		for (final var bone : bones) {
			final var nodeIdx = bone2Node.get(bone.getBoneIndex());
			var parentNode = rootNode;

			if (bone.hasParentBone()) {
				final var parentBoneIdx = bone.getParentBoneReference();
				parentNode = this.gltfModel.getNodes().get(bone2Node.get(parentBoneIdx));
			}

			parentNode.addChildren(nodeIdx);

			final var translation = new float[] { bone.getLocationX(), bone.getLocationY(), bone.getLocationZ() };
			if (bone.hasParentBone()) {
				// gltf calculates position along the edges, while WS uses absolute position
				final var parentBone = bones.get(bone.getParentBoneReference());
				translation[0] -= parentBone.getLocationX();
				translation[1] -= parentBone.getLocationY();
				translation[2] -= parentBone.getLocationZ();
			}

			final var boneNode = this.gltfModel.getNodes().get(nodeIdx);
			boneNode.setTranslation(translation);
			// boneNode.setRotation(MathUtil.toQuaternion(bone.getTransformationMatrix()));

			// works, but using the inverse transformation matrix for the inverse binding matrix gives weird results
			// boneNode.setMatrix(bone.getTransformationMatrix());

			// Calculate pre-multiplied transformation matrix
			// mB = mT * mP (mB current matrix, mT new, mP parent matrix)
			// mP^(-1) * mB = mT

			// {
			// final var mat1 = bone.getTransformationMatrix(0);
			// final var mat2 = bone.getTransformationMatrix(1);
			// final var mat3 = multiply(mat1, mat2);
			// System.out.println(mat3);
			// }
		}
	}

	private void writeMeshData() throws IOException {
		final var modelGeometry = this.model.getGeometry();
		final var meshes = modelGeometry.getMeshes();

		if (meshes.isEmpty()) {
			return; // done
		}

		final var meshWriter = new MeshWriter(this.model);
		for (final var mesh : meshes) {
			writeMeshData(mesh, meshWriter);
		}
	}

	private void writeMeshData(ModelMesh mesh, MeshWriter meshWriter) throws IOException {
		// write binary data
		final var geometryWrite = writeToBinary((writer, position) -> {
			meshWriter.writeGeometry(mesh, writer);
		});

		final var indexWrite = writeToBinary((writer, position) -> {
			meshWriter.writeIndices(mesh, writer);
		});

		// create geometry and indices view
		final var geoBufferViewIdx = GltfHelper.addBufferView(this.gltfModel, geometryWrite.writeStart, geometryWrite.writeLength);
		final var geoBufferView = this.gltfModel.getBufferViews().get(geoBufferViewIdx);
		geoBufferView.setByteStride(meshWriter.getVertexWriteSize());
		geoBufferView.setName(String.format("bufferview_geo_%d", mesh.getMeshIndex()));

		final var indicesViewIdx = GltfHelper.addBufferView(this.gltfModel, indexWrite.writeStart, indexWrite.writeLength);
		final var indicesView = this.gltfModel.getBufferViews().get(indicesViewIdx);
		indicesView.setName(String.format("bufferview_idx_%d", mesh.getMeshIndex()));

		// create view accessors
		final var accessorCount = GltfHelper.getAccessorCount(this.gltfModel);
		final var fieldAccessors = meshWriter.getVertexFields(); // Ids will start here and use fieldAccessor.length + 1 (indices) numbers

		for (final var field : fieldAccessors) {
			final var idx = GltfHelper.addAccessor(this.gltfModel, field, validateIntegerLimit(mesh.getVertexCount()));
			final var accessor = this.gltfModel.getAccessors().get(idx);
			accessor.setName(String.format("accessor_%d_%s", mesh.getMeshIndex(), field.getNameShort()));
			accessor.setBufferView(geoBufferViewIdx);
		}

		final var indicesAccessorIdx = GltfHelper.addAccessor(this.gltfModel, GlTFType.SCALAR, GlTFComponentType.UINT32);
		final var indicesAccessor = this.gltfModel.getAccessors().get(indicesAccessorIdx);
		indicesAccessor.setName(String.format("accessor_idx_%d", mesh.getMeshIndex()));
		indicesAccessor.setBufferView(indicesViewIdx);
		indicesAccessor.setCount(validateIntegerLimit(mesh.getIndexCount()));
		indicesAccessor.setMax(new Number[] { meshWriter.getIndexUpperBound() });
		indicesAccessor.setMin(new Number[] { meshWriter.getIndexLowerBound() });

		// create mesh primitives and link it to accessors
		final var gltfMeshPrimitive = new MeshPrimitive();
		for (int i = 0; i < fieldAccessors.length; ++i) {
			final var field = fieldAccessors[i];
			gltfMeshPrimitive.addAttributes(field.getAttributeKey(), accessorCount + i);
		}

		gltfMeshPrimitive.setMode(GlTFMode.TRIANGLES.getId());
		gltfMeshPrimitive.setIndices(accessorCount + fieldAccessors.length);
		if (isExportTextures()) {
			gltfMeshPrimitive.setMaterial(mesh.getMaterialReference());
		}

		final var nameSuffix = computeMeshNodeSuffix(mesh);

		// create mesh with mesh primitive
		final var gltfMeshIdx = GltfHelper.addMesh(this.gltfModel);
		final var gltfMesh = this.gltfModel.getMeshes().get(gltfMeshIdx);

		gltfMesh.setName("mesh" + nameSuffix);
		gltfMesh.addPrimitives(gltfMeshPrimitive);

		// add mesh to a new node and add node to scene
		final var gltfNodeIdx = GltfHelper.getNodeCount(this.gltfModel);
		final var gltfNode = new Node();
		gltfNode.setMesh(mesh.getMeshIndex());
		gltfNode.setName("node" + nameSuffix);
		if (isExportSkeleton()) {
			gltfNode.setSkin(0);
		}

		this.gltfModel.addNodes(gltfNode);
		GltfHelper.getRootNode(this.gltfModel).addChildren(gltfNodeIdx);
	}

	private void writeTextureData() throws IOException {
		if (this.textureManager == null) {
			this.textureManager = new TextureManager(getOutputDirectory(), this.gltfModel, this.monitor);
		}

		for (final var textures : this.model.getTextures()) {
			final var texturePath = textures.getTexturePath();
			this.textureManager.loadTextureIntoModel(texturePath);
		}

	}

	private void writeMaterialData() throws IOException {
		final var textures = this.model.getTextures();
		final var materials = this.model.getMaterials();

		for (final var material : materials) {
			final var matDescriptions = material.getMaterialDescriptions();
			final var gltfMaterial = new Material();
			this.gltfModel.addMaterials(gltfMaterial);

			for (final var matDescription : matDescriptions) {
				if (textures.size() > matDescription.getTextureReferenceA()) {
					final var refTexture = textures.get(matDescription.getTextureReferenceA());
					final var gltfTexIndices = this.textureManager.getGltfTextureIndices(refTexture.getTexturePath());
					if (gltfTexIndices.length != 0) {
						final var gltfMatDescription = new MaterialPbrMetallicRoughness();

						{ // diffuse
							final var gltfTexInfo = new TextureInfo();
							gltfTexInfo.setIndex(gltfTexIndices[0]);
							gltfMatDescription.setBaseColorTexture(gltfTexInfo);
						}

						if (gltfTexIndices.length >= 2) { // Roughness (in some cases?)
							final var gltfTexInfo = new TextureInfo();
							gltfTexInfo.setIndex(gltfTexIndices[1]);
							gltfMatDescription.setMetallicRoughnessTexture(gltfTexInfo);
						}

						gltfMaterial.setPbrMetallicRoughness(gltfMatDescription);
					}
				}

				if (textures.size() > matDescription.getTextureReferenceB()) {
					final var refTexture = textures.get(matDescription.getTextureReferenceB());
					final var gltfTexIndices = this.textureManager.getGltfTextureIndices(refTexture.getTexturePath());

					if (gltfTexIndices.length > 0) { // normal
						final MaterialNormalTextureInfo normal = new MaterialNormalTextureInfo();
						normal.setIndex(gltfTexIndices[0]);
						gltfMaterial.setNormalTexture(normal);
					}

					if (gltfTexIndices.length > 1) { // occlusion (sometimes?)
						final MaterialOcclusionTextureInfo occlusion = new MaterialOcclusionTextureInfo();
						occlusion.setIndex(gltfTexIndices[1]);
						gltfMaterial.setOcclusionTexture(occlusion);
					}

					if (gltfTexIndices.length > 2) { // emissive (sometimes?)
						final TextureInfo gltfTexInfo = new TextureInfo();
						gltfTexInfo.setIndex(gltfTexIndices[2]);
						gltfMaterial.setEmissiveTexture(gltfTexInfo);
						gltfMaterial.setEmissiveFactor(new float[] { 1f, 1f, 1f });
					}
				}
			}
		}
	}

	private String computeMeshNodeSuffix(ModelMesh mesh) {
		return String.format("_%s_%d", this.modelName, mesh.getMeshIndex());
	}

	private static interface WriteJob {
		void task(BinaryWriter writer, long position) throws IOException;
	}

	private static class WriteResult {
		public WriteResult(int start, int length) {
			this.writeStart = start;
			this.writeLength = length;
		}

		public final int writeStart;
		public final int writeLength;
	}

	private WriteResult writeToBinary(WriteJob fun) throws IOException {
		long writeStart = 0;
		long writeEnd = 0;
		long length = 0;

		try (var channel = getBinaryChannel()) {
			try (var writer = getBinaryChannelWriter(channel)) {
				writeStart = channel.position();
				fun.task(writer, writeStart);
				writer.flush();
				writeEnd = channel.position();
				length = channel.size();
			}
		}

		updateGltfBuffer(validateIntegerLimit(length));
		return new WriteResult(validateIntegerLimit(writeStart), validateIntegerLimit(writeEnd - writeStart));
	}

	private void updateGltfBuffer(int length) {
		if (GltfHelper.getBufferCount(this.gltfModel) == 0) {
			final var bufferUri = getBinaryFile().getFileName().toString();
			GltfHelper.addBuffer(this.gltfModel, bufferUri, length);
			this.gltfModel.getBuffers().get(0).setName("binary_buffer");
		} else {
			this.gltfModel.getBuffers().get(0).setByteLength(length);
		}
	}

	private SeekableByteChannel getBinaryChannel() throws IOException {
		ensureBinaryIsAvailable();
		return Files.newByteChannel(getBinaryFile(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
	}

	private BinaryWriter getBinaryChannelWriter(WritableByteChannel channel) throws IOException {
		final var buffer = ByteBuffer.allocateDirect(1024 * 1024 * 1).order(ByteOrder.LITTLE_ENDIAN);
		return new WritableByteChannelBinaryWriter(channel, buffer);
	}

	// TODO
	private int validateIntegerLimit(long value) {
		if (value < 0 || value > Integer.MAX_VALUE) {
			throw new IntegerOverflowException(String.format("jglTF only supports values within a size of %d bytes", Integer.MAX_VALUE));
		}
		return (int) value;
	}

	private void ensureBinaryIsAvailable() throws IOException {
		if (this.binaryBufferFile == null) {
			final String binaryName = getModelName() + ".bin";
			this.binaryBufferFile = getOutputDirectory().resolve(binaryName);

			Files.deleteIfExists(this.binaryBufferFile);
			Files.createFile(this.binaryBufferFile);
			logFileCreation(this.binaryBufferFile);
		}
	}

	private Path getBinaryFile() {
		return this.binaryBufferFile;
	}

	private Path getOutputDirectory() {
		return this.outputDirectory;
	}

	private String getModelName() {
		return this.modelName;
	}

	private void writeGltf() throws IOException {
		Files.createDirectories(getOutputDirectory());
		final GltfAsset gltfAsset = new GltfAssetV2(this.gltfModel, null);
		final GltfModel gltfModel = GltfModels.create(gltfAsset);
		final GltfModelWriter gltfModelWriter = new GltfModelWriter();
		final Path outputFile = getOutputDirectory().resolve(getModelName() + ".gltf");
		gltfModelWriter.write(gltfModel, outputFile.toFile());
		logFileCreation(outputFile);
	}

	private void logFileCreation(Path path) {
		if (this.monitor != null) {
			try {
				this.monitor.newFileCreated(path);
			} catch (final Throwable t) {
				throw new OnAddFileException(t);
			}
		}
	}

}
