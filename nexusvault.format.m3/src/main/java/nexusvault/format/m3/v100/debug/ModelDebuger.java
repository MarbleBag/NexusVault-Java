package nexusvault.format.m3.v100.debug;

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
import nexusvault.format.m3.v100.DataTracker;
import nexusvault.format.m3.v100.struct.StructM3Header;

public final class ModelDebuger {

	private static DataTracker extractDataFromModel(Model m3) {
		final Field[] fields = m3.getClass().getDeclaredFields();
		for (final Field field : fields) {
			if (field.getName().equals("modelData")) {
				final boolean accessible = field.isAccessible();
				field.setAccessible(true);
				try {
					final DataTracker tracker = (DataTracker) field.get(m3);
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
		final DataTracker modelData = extractDataFromModel(m3);
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
			return obj;
		}
	}

	private final StructReader<ByteBuffer> structBuilder = StructReader.build(StructFactory.build(), DataReadDelegator.build(new ByteBufferReader()), true);

	private Queue<Task> taskQueue;
	private DataTracker modelData;

	private final Class2ObjectLookup<StructFormater> sturctFormaters;

	public ModelDebuger() {
		sturctFormaters = new Class2ObjectLookup<>(null);
		sturctFormaters.setLookUp(Object.class, new BasicStructFormater());
	}

	public Table debugModel(Model m3) {
		final DataTracker modelData = extractDataFromModel(m3);
		modelData.resetPosition();
		return debugModel(modelData, StructM3Header.class, 1);
	}

	protected DataTracker getDataModel() {
		return this.modelData;
	}

	protected List<Object> loadStructs(int dataOffset, Class<?> structClass, int structCount) {
		final DataTracker data = getDataModel();
		data.setPosition(dataOffset);
		return loadStructs(structClass, structCount, getDataModel());
	}

	protected List<Object> loadStructs(Class<?> structClass, int structCount, DataTracker data) {
		final List<Object> structs = new ArrayList<>(structCount);
		for (int i = 0; i < structCount; ++i) {
			structs.add(structBuilder.read(structClass, data.getData()));
		}
		return structs;
	}

	public void setStructFormater(Class<?> structClass, StructFormater formater) {
		sturctFormaters.setLookUp(structClass, formater);
	}

	protected StructFormater getInternalStructFormater(Class<?> structClass) {
		StructFormater formater = sturctFormaters.getLookUp(structClass);
		if (formater == null) {
			formater = new BasicStructFormater();
			sturctFormaters.setLookUp(structClass, formater);
		}
		return formater;
	}

	public Table debugModel(DataTracker modelData, Class<?> structClass, int structCount) {
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
		taskQueue.add(task);
	}

	private void runTasks() {
		final Util debuger = new Util() {

			@Override
			public DataTracker getDataModel() {
				return ModelDebuger.this.getDataModel();
			}

			@Override
			public List<Object> loadStructs(long dataOffset, Class<?> structClass, int structCount) {
				modelData.setPosition(dataOffset);
				return ModelDebuger.this.loadStructs(structClass, structCount, modelData);
			}

			@Override
			public StructFormater getStructFormater(Class<?> structClass) {
				return ModelDebuger.this.getInternalStructFormater(structClass);
			}

			@Override
			public void queueTask(Task task) {
				ModelDebuger.this.queueTask(task);
			}

		};

		while (!this.taskQueue.isEmpty()) {
			final Task task = taskQueue.poll();
			task.runTask(debuger);
		}
	}

}
