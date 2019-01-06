package nexusvault.format.m3.export.gltf;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;

import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.BufferModel;
import de.javagl.jgltf.model.BufferViewModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.io.GltfModelReader;

class Importer {

	public void importModel(Path path) throws IOException {

		final GltfModelReader gltfReader = new GltfModelReader();
		final GltfModel gltfModel = gltfReader.read(path.toUri());

		System.out.println("GLTF " + path);

		System.out.println("Buffers");
		if (gltfModel.getBufferModels() != null) {
			for (final BufferModel bufferModel : gltfModel.getBufferModels()) {
				System.out.println("Buffer: " + bufferModel.getName() + " : " + bufferModel.getByteLength() + " : " + bufferModel.getUri());
			}
		} else {
			System.out.println("No buffers found");
		}

		System.out.println("Buffer views");
		if (gltfModel.getBufferViewModels() != null) {
			for (final BufferViewModel bufferViewModel : gltfModel.getBufferViewModels()) {
				System.out.println("BufferView: " + bufferViewModel.getName() + " : " + bufferViewModel.getByteOffset() + " : "
						+ bufferViewModel.getByteLength() + " : " + bufferViewModel.getByteStride() + " -> " + bufferViewModel.getBufferModel().getName());
			}
		} else {
			System.out.println("No buffer views found");
		}

		System.out.println("Accessor");
		if (gltfModel.getAccessorModels() != null) {
			for (final AccessorModel accessor : gltfModel.getAccessorModels()) {
				System.out.println("Accessor: " + accessor.getName() + " : offset" + accessor.getByteOffset() + " : stride" + accessor.getByteStride()
						+ " : count" + accessor.getCount() + " : sizepercomponent" + accessor.getComponentSizeInBytes() + " : comp"
						+ accessor.getComponentType() + " : type" + accessor.getElementType() + " -> " + accessor.getBufferViewModel().getName());
			}
		} else {
			System.out.println("No Accessors found");
		}

		System.out.println("Nodes");
		if (gltfModel.getNodeModels() != null) {
			for (final NodeModel node : gltfModel.getNodeModels()) {
				System.out.println("Nodes: " + node.getName());
				if (node.getMeshModels() != null) {
					for (final MeshModel mesh : node.getMeshModels()) {
						System.out.print("Mesh: " + mesh.getName());
						for (final MeshPrimitiveModel meshPrimitives : mesh.getMeshPrimitiveModels()) {
							System.out.print(" index->" + meshPrimitives.getIndices().getName());
							for (final Entry<String, AccessorModel> s : meshPrimitives.getAttributes().entrySet()) {
								System.out.print(" " + s.getKey() + "->" + s.getValue().getName());
							}
						}
						System.out.println();
					}
				}
			}

		} else {
			System.out.println("No Meshes found");
		}

	}

}
