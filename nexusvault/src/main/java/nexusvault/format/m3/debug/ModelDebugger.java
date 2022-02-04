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

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import kreed.reflection.struct.DataReadDelegator;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructReader;
import kreed.reflection.struct.reader.ByteBufferReader;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.struct.StructGeometry;
import nexusvault.format.m3.struct.StructM3Header;
import nexusvault.format.m3.struct.StructTexture;

public final class ModelDebugger {

	private static BytePositionTracker extractDataFromModel(Model m3) {
		final Field[] fields = m3.getClass().getDeclaredFields();
		for (final Field field : fields) {
			if (field.getName().equals("modelData")) {
				final boolean accessible = field.isAccessible();
				field.setAccessible(true);
				try {
					final BytePositionTracker tracker = (BytePositionTracker) field.get(m3);
					return tracker;
				} catch (final IllegalAccessException e) {

				} finally {
					field.setAccessible(accessible);
				}
			}
		}

		throw new IllegalArgumentException("Unable to debug model. Type " + m3.getClass() + " not supported.");
	}

	public static ByteBuffer getByteBuffer(Model m3) {
		final BytePositionTracker modelData = extractDataFromModel(m3);
		modelData.resetPosition();
		final ByteBuffer buffer = modelData.getData();
		return buffer;
	}

	private static class TaskOutputStore<T> implements TaskOutput<T> {
		private T obj;

		@Override
		public void setOutput(T out) {
			this.obj = out;
		}

		public T getOutput() {
			return this.obj;
		}
	}

	public static ModelDebugger createDefaultModelDebugger() {
		final ModelDebugger debugger = new ModelDebugger();

		{
			final BasicStructFormater formater = new BasicStructFormater();
			formater.setFieldFormater("vertexBlockData", new VertexBlockFieldFormater());
			formater.setFieldFormater("indexData", new IgnoreFieldFormater());
			debugger.setStructFormater(StructGeometry.class, formater);
		}

		{
			final BasicStructFormater formater = new BasicStructFormater();
			formater.setFieldFormater("textureName", new NullTerminatedStringFieldFormater());
			debugger.setStructFormater(StructTexture.class, formater);
		}

		return debugger;
	}

	private final StructReader<ByteBuffer> structBuilder = StructReader.build(StructFactory.build(), DataReadDelegator.build(new ByteBufferReader()), true);

	private final Class2ObjectLookup<StructFormater> sturctFormaters;

	private Queue<Task> taskQueue;
	private BytePositionTracker modelData;

	public ModelDebugger() {
		this.sturctFormaters = new Class2ObjectLookup<>(null);
		this.sturctFormaters.setLookUp(Object.class, new BasicStructFormater());
	}

	protected BytePositionTracker getDataModel() {
		return this.modelData;
	}

	protected List<Object> loadStructs(int dataOffset, Class<?> structClass, int structCount) {
		final BytePositionTracker data = getDataModel();
		data.setPosition(dataOffset);
		return loadStructs(structClass, structCount, getDataModel());
	}

	protected List<Object> loadStructs(Class<?> structClass, int structCount, BytePositionTracker data) {
		final List<Object> structs = new ArrayList<>(structCount);
		for (int i = 0; i < structCount; ++i) {
			structs.add(this.structBuilder.read(structClass, data.getData()));
		}
		return structs;
	}

	public void setStructFormater(Class<?> structClass, StructFormater formater) {
		this.sturctFormaters.setLookUp(structClass, formater);
	}

	protected StructFormater getInternalStructFormater(Class<?> structClass) {
		final StructFormater formater = this.sturctFormaters.getLookUp(structClass);
		// if (formater == null) {
		// formater = new BasicStructFormater();
		// sturctFormaters.setLookUp(structClass, formater);
		// }
		return formater;
	}

	public Table debugModel(Model m3) {
		final BytePositionTracker modelData = extractDataFromModel(m3);
		modelData.resetPosition();
		return debugModel(modelData, StructM3Header.class, 1);
	}

	public Table debugModel(BytePositionTracker modelData, Class<?> structClass, int structCount) {
		this.modelData = modelData;
		this.taskQueue = new LinkedList<>();

		final TaskOutputStore<Table> tableStore = new TaskOutputStore<>();
		queueTask(new StructFormatTask(0, structClass, 1, tableStore));

		try {
			runTasks();
		} finally {
			this.modelData = null;
		}

		final Table table = tableStore.getOutput();
		return table;
	}

	private void queueTask(Task task) {
		if (task == null) {
			throw new IllegalArgumentException("'task' must not be null");
		}
		this.taskQueue.add(task);
	}

	private void runTasks() {
		final DebugInfo debugger = new DebugInfo() {

			@Override
			public BytePositionTracker getDataModel() {
				return ModelDebugger.this.getDataModel();
			}

			@Override
			public List<Object> loadStructs(long dataOffset, Class<?> structClass, int structCount) {
				ModelDebugger.this.modelData.setPosition(dataOffset);
				return ModelDebugger.this.loadStructs(structClass, structCount, ModelDebugger.this.modelData);
			}

			@Override
			public StructFormater getStructFormater(Class<?> structClass) {
				return ModelDebugger.this.getInternalStructFormater(structClass);
			}

			@Override
			public void queueTask(Task task) {
				ModelDebugger.this.queueTask(task);
			}

		};

		while (!this.taskQueue.isEmpty()) {
			final Task task = this.taskQueue.poll();
			task.runTask(debugger);
		}
	}

}
