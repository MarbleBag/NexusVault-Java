/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.m3.debug;

import java.util.LinkedList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.m3.debug.Table.TableCell;
import nexusvault.format.m3.debug.Table.TableColumn;
import nexusvault.format.m3.debug.Table.TableRow;
import nexusvault.format.m3.struct.StructGeometry;
import nexusvault.format.m3.struct.StructGeometry.VertexFieldOld;
import nexusvault.format.m3.struct.StructMesh;

final class VertexBlockFieldFormater implements FieldFormater {

	@Override
	public void processField(DebugInfo debugger, TableCell cell, FieldReader fieldReader) {
		final StructGeometry geometry = (StructGeometry) fieldReader.getObject();
		debugger.queueTask(new VertexBlockFieldTask(geometry, (in) -> cell.addEntry(in)));
	}

	private static final class VertexBlockFieldTask implements Task {

		private final StructGeometry geometry;
		private final TaskOutput<? super Table> out;

		private List<VertexFieldOld> vertexFields;

		public VertexBlockFieldTask(StructGeometry geometry, TaskOutput<? super Table> out) {
			this.geometry = geometry;
			this.out = out;
		}

		@Override
		public void runTask(DebugInfo debugger) {
			try {
				final Table table = createInitialTable();
				processVertices(table, debugger);
				this.out.setOutput(table);
			} finally {
				this.vertexFields = null;
			}
		}

		private Table createInitialTable() {
			final List<TableColumn> columns = new LinkedList<>();
			this.vertexFields = new LinkedList<>();
			columns.add(new TableColumn("Mesh #", "Mesh", null));
			for (final VertexFieldOld vertexField : VertexFieldOld.values()) {
				if (!this.geometry.isVertexFieldAvailable(vertexField)) {
					continue;
				}
				this.vertexFields.add(vertexField);
				final TableColumn column = new TableColumn(vertexField.name(), vertexField.name(), null);
				column.setArrayAnnotation(vertexField.getValueCount());
				columns.add(column);
			}
			return new Table(columns);
		}

		@SuppressWarnings("unchecked")
		private void processVertices(Table table, DebugInfo debugger) {

			final List<StructMesh> meshes = (List) debugger.loadStructs(this.geometry.meshes.getOffset(), this.geometry.meshes.getTypeOfElement(),
					this.geometry.meshes.getArrayLength());

			final int vertexBlockSize = this.geometry.vertexBlockSizeInBytes;
			final BinaryReader modelData = new ByteBufferBinaryReader(debugger.getDataModel().getData());

			for (int i = 0; i < meshes.size(); ++i) {
				final StructMesh mesh = meshes.get(i);
				final long vertexBlockStart = this.geometry.vertexBlockData.getOffset() + mesh.startVertex * vertexBlockSize;

				for (int j = 0; j < mesh.vertexCount; ++j) {
					final long vertexPosition = vertexBlockStart + j * vertexBlockSize;
					modelData.seek(Seek.BEGIN, vertexPosition);

					final TableRow newRow = table.addNewRow();

					table.getColumn("Mesh").getCell(newRow).addEntry(String.format("Mesh %d", i));

					for (final VertexFieldOld vertexField : this.vertexFields) {
						final TableColumn column = table.getColumn(vertexField.name());
						final TableCell cell = column.getCell(newRow);

						switch (vertexField) {
							case LOCATION:
								switch (this.geometry.getVertexFieldLocationType()) {
									case INT16:
										cell.addEntry(modelData.readInt16());
										cell.addEntry(modelData.readInt16());
										cell.addEntry(modelData.readInt16());
										break;
									case FLOAT32:
										cell.addEntry(modelData.readFloat32());
										cell.addEntry(modelData.readFloat32());
										cell.addEntry(modelData.readFloat32());
										break;
								}
								break;
							case FIELD_3_UNK_1:
							case FIELD_3_UNK_2:
							case FIELD_3_UNK_3:
								cell.addEntry(modelData.readInt8());
								cell.addEntry(modelData.readInt8());
								break;
							case BONE_MAP:
							case BONE_WEIGHTS:
								cell.addEntry(modelData.readUInt8());
								cell.addEntry(modelData.readUInt8());
								cell.addEntry(modelData.readUInt8());
								cell.addEntry(modelData.readUInt8());
								break;
							case FIELD_4_UNK_1:
								cell.addEntry(modelData.readInt8());
								cell.addEntry(modelData.readInt8());
								cell.addEntry(modelData.readInt8());
								cell.addEntry(modelData.readInt8());
								break;
							case FIELD_4_UNK_2:
								cell.addEntry(modelData.readInt8());
								cell.addEntry(modelData.readInt8());
								cell.addEntry(modelData.readInt8());
								cell.addEntry(modelData.readInt8());
								break;
							case UV_MAP_1:
							case UV_MAP_2:
								cell.addEntry(modelData.readFloat16());
								cell.addEntry(modelData.readFloat16());
								break;
							case FIELD_6_UNK_1:
								cell.addEntry(modelData.readInt8());
								break;
						}
					}
				}
			}
		}

	}
}