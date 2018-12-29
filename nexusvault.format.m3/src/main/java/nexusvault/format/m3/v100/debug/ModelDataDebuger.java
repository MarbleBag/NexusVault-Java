package nexusvault.format.m3.v100.debug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.function.Consumer;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.FieldExtractor;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.DataTracker;
import nexusvault.format.m3.v100.ModelDataRaw;
import nexusvault.format.m3.v100.pointer.ArrayTypePointer;
import nexusvault.format.m3.v100.pointer.DoubleArrayTypePointer;
import nexusvault.format.m3.v100.struct.StructM3Header;

public class ModelDataDebuger {

	private static <T> T getFieldData(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		final Field field = obj.getClass().getField(fieldName);
		final boolean wasAccessible = field.isAccessible();
		field.setAccessible(true);
		final Object result = field.get(obj);
		field.setAccessible(wasAccessible);
		return (T) result;
	}

	private static final String INT_32_FORMAT = "% 6d";

	private static void forEachFieldIn(Object struct, Consumer<Field> consumer) {
		forEachFieldIn(struct.getClass(), consumer);
	}

	private static void forEachFieldIn(Class<?> structClazz, Consumer<Field> consumer) {
		final List<Field> fields = FieldExtractor.getStructFields(structClazz, false);
		for (final Field field : fields) {
			final boolean wasAccessible = field.isAccessible();
			field.setAccessible(true);
			try {
				consumer.accept(field);
			} finally {
				field.setAccessible(wasAccessible);
			}
		}
	}

	public static class ToDo {
		private final String name;
		private final Object data;

		public ToDo(String name, Object data) {
			this.name = name;
			this.data = data;
		}

		public String getName() {
			return name;
		}

		public Object getData() {
			return data;
		}
	}

	public static void swat(DataTracker data, Class<?> structClass, int number, Path base, String id) {
		if (number == 0) {
			return;
		}

		final Queue<ToDo> components = new LinkedList<>();
		swat(data, structClass, number, base, id, components, null, true);

		while (!components.isEmpty()) {
			try {
				final ToDo next = components.poll();
				final Object nextObj = next.getData();

				if (nextObj instanceof ArrayTypePointer) {
					final ArrayTypePointer pointer = (ArrayTypePointer) nextObj;
					data.setPosition(pointer.getOffset());
					if (pointer.hasType()) {
						swat(data, pointer.getTypeOfElement(), pointer.getArraySize(), base, next.getName(), components, null, true);
					} else {
						swat(data, pointer.getElementSize(), pointer.getArraySize(), base, next.getName());
					}
				} else if (nextObj instanceof DoubleArrayTypePointer) {
					final DoubleArrayTypePointer pointer = (DoubleArrayTypePointer) nextObj;
					swat(data, pointer, base, next.getName());
				}

			} catch (final Throwable e) {
				// TODO
				e.printStackTrace();
			}
		}
	}

	public static void swat(DataTracker data, DoubleArrayTypePointer pointer, Path base, String id) {
		if (pointer.getArraySize() == 0) {
			return;
		}

		data.setPosition(pointer.getOffsetA());
		final List<byte[]> structsA = new ArrayList<>(pointer.getArraySize());
		for (int i = 0; i < pointer.getArraySize(); ++i) {
			final byte[] arr = new byte[pointer.getElementSizeA()];
			data.getData().get(arr);
			structsA.add(arr);
		}

		data.setPosition(pointer.getOffsetB());
		final List<byte[]> structsB = new ArrayList<>(pointer.getArraySize());
		for (int i = 0; i < pointer.getArraySize(); ++i) {
			final byte[] arr = new byte[pointer.getElementSizeB()];
			data.getData().get(arr);
			structsB.add(arr);
		}

		final String elementFormat = "% 4d";
		final String rowFormat = "% " + String.valueOf(pointer.getArraySize()).length() + "d";
		final String sizeFormatA = "% " + String.valueOf(structsA.size()).length() + "d";
		final String sizeFormatB = "% " + String.valueOf(structsB.size()).length() + "d";

		final Path currentFile = base.resolve(id + ".txt");
		try (BufferedWriter writer = Files.newBufferedWriter(currentFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {

			for (int i = 0; i < pointer.getArraySize(); ++i) {
				writer.append(String.format(rowFormat, i)).append(" ");
				{
					final byte[] a = structsA.get(i);
					writer.append("(").append(String.format(sizeFormatA, a.length)).append(") [");
					for (int j = 0; j < a.length; ++j) {
						if (j != 0) {
							writer.append(", ");
						}
						writer.append(String.format(elementFormat, a[j]));
					}
					writer.append("] ");
				}

				{
					final byte[] b = structsB.get(i);
					writer.append("(").append(String.format(sizeFormatB, b.length)).append(") [");
					for (int j = 0; j < b.length; ++j) {
						if (j != 0) {
							writer.append(", ");
						}
						writer.append(String.format(elementFormat, b[j]));
					}
					writer.append("]\n");
				}
			}
		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void swat(DataTracker data, int byteSize, int number, Path base, String id) {
		if (number == 0) {
			return;
		}

		final List<byte[]> structs = new ArrayList<>(number);
		for (int i = 0; i < number; ++i) {
			final byte[] b = new byte[byteSize];
			data.getData().get(b);
			structs.add(b);
		}

		final String elementFormat = "% 4d";
		final String sizeFormat = "% " + String.valueOf(byteSize).length() + "d";
		final String rowFormat = "% " + String.valueOf(number).length() + "d";

		final Path currentFile = base.resolve(id + ".txt");
		try (BufferedWriter writer = Files.newBufferedWriter(currentFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			for (int i = 0; i < structs.size(); ++i) {
				final byte[] b = structs.get(i);
				writer.append(String.format(rowFormat, i)).append(" ");
				writer.append("(").append(String.format(sizeFormat, b.length)).append(") [");
				for (int j = 0; j < byteSize; ++j) {
					if (j != 0) {
						writer.append(", ");
					}
					writer.append(String.format(elementFormat, b[j]));
				}
				writer.append("]\n");
			}
		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void swat(DataTracker data, Class<?> structClass, int number, Path base, String id, Collection<ToDo> components, String origin,
			boolean overwrite) {
		if (number == 0) {
			return;
		}

		final List<Object> structs = new LinkedList<>();
		for (int i = 0; i < number; ++i) {
			structs.add(StructUtil.readStruct(structClass, data.getData(), false));
		}

		final List<Field> fields = FieldExtractor.getStructFields(structClass, false);
		final boolean[] fieldAccess = new boolean[fields.size()];
		for (int i = 0; i < fieldAccess.length; ++i) {
			fieldAccess[i] = fields.get(i).isAccessible();
		}

		try {
			for (int i = 0; i < fieldAccess.length; ++i) {
				fields.get(i).setAccessible(true);
			}

			final PrintFormat[] printFormats = new PrintFormat[fields.size()];
			for (int i = 0; i < fields.size(); i++) {
				final Field field = fields.get(i);
				printFormats[i] = findBestPrintFormat(field, structs);
			}

			// TODO
			final Path currentFile = base.resolve(id + ".txt");

			// TODO
			OpenOption[] writerOptions = null;
			if (overwrite) {
				writerOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING };
			} else {
				writerOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND };
			}
			if (Files.exists(currentFile)) {
				final long fileSize = Files.size(currentFile);
				if (!overwrite && (fileSize > (128 * 1024 * 1024))) {
					return;
				}
			}

			try (BufferedWriter writer = Files.newBufferedWriter(currentFile, writerOptions)) {

				for (int i = 0; i < structs.size(); ++i) {
					if (i != 0) {
						writer.append("\n");
					}

					if (origin != null) {
						writer.append(String.format("%1$-" + 70 + "s", origin)).append(" ");
					}

					final Object struct = structs.get(i);
					for (int j = 0; j < fields.size(); j++) {
						if (j != 0) {
							writer.append(", ");
						}

						final Field field = fields.get(j);
						final Class<?> fieldType = field.getType().isArray() ? field.getType().getComponentType() : field.getType();
						final Get fieldGet = getGet(struct, field);
						final StructField structInfo = field.getAnnotation(StructField.class);

						// TODO print field name
						writer.append(field.getName()).append("=");

						if (!DataType.STRUCT.equals(structInfo.value())) { // process primitives (non-structures)
							if (fieldGet.isArray()) {
								// TODO size format is not yet unificated
								writer.append("(").append(String.valueOf(fieldGet.size())).append(")").append("[");
							}

							for (int k = 0; k < fieldGet.size(); ++k) {
								if (k != 0) {
									writer.append(", ");
								}
								final PrintFormat printFormat = printFormats[j];
								printFormat.print(writer, fieldGet.get());
							}

							if (fieldGet.isArray()) {
								writer.append("]");
							}
						} else {
							final String structIndexNumberFormat = "%" + String.valueOf(structs.size()).length() + "d";
							final String nId = id + "_" + String.format(structIndexNumberFormat, i) + "." + field.getName();
							if (fieldGet.isArray()) {
								writer.append("(").append(String.valueOf(fieldGet.size())).append(")").append("[");
							}

							final Object[] fieldValues = new Object[fieldGet.size()];
							for (int k = 0; k < fieldGet.size(); ++k) {
								fieldValues[k] = fieldGet.get();
							}

							int maxElements = 0;
							for (final Object o : fieldValues) {
								if (o instanceof ArrayTypePointer) {
									maxElements = Math.max(maxElements, ((ArrayTypePointer) o).getArraySize());
								} else if (o instanceof DoubleArrayTypePointer) {
									maxElements = Math.max(maxElements, ((DoubleArrayTypePointer) o).getArraySize());
								}
							}

							// TODO not good, just handles the case for the current struct, not all of them.
							final String indexFileFormat = "%0" + String.valueOf(fieldGet.size()).length() + "d";
							final String indexNumberFormat = "%" + String.valueOf(maxElements).length() + "d";

							for (int k = 0; k < fieldGet.size(); ++k) {
								if (k != 0) {
									writer.append(", ");
								}
								final String fId = nId + "_" + String.format(indexFileFormat, k);
								final Object fieldValue = fieldValues[k];

								if (fieldValue == null) {
									writer.append("null");
								} else if (ArrayTypePointer.class.isAssignableFrom(fieldType)) {
									final ArrayTypePointer pointer = (ArrayTypePointer) fieldValue;
									writer.append("(").append(String.format(indexNumberFormat, pointer.getArraySize())).append(")").append(fId);
									components.add(new ToDo(fId, pointer));
								} else if (DoubleArrayTypePointer.class.isAssignableFrom(fieldType)) {
									final DoubleArrayTypePointer pointer = (DoubleArrayTypePointer) fieldValue;
									writer.append("(").append(String.format(indexNumberFormat, pointer.getArraySize())).append(")").append(fId);
									components.add(new ToDo(fId, pointer));
								} else {
									writer.append(StructUtil.toString(fieldValue));
								}
							}

							if (fieldGet.isArray()) {
								writer.append("]");
							}
						}
					}
				}
				// TODO (maybe)
				writer.append("\n");
			}
		} catch (IOException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} finally {
			// TODO not perfect
			for (int i = 0; i < fieldAccess.length; ++i) {
				fields.get(i).setAccessible(fieldAccess[i]);
			}
		}
	}

	private static Get getGet(Object struct, Field field) throws IllegalArgumentException, IllegalAccessException {
		final boolean isArray = field.getType().isArray();
		final Object fieldValue = field.get(struct); // no care for primitives
		if (isArray) {
			return new ArrayGet(fieldValue);
		} else {
			return new SimpleGet(fieldValue);
		}
	}

	private static final class PrintFormat {
		private final String formatString;

		public PrintFormat(String formatString) {
			this.formatString = formatString;
		}

		public void print(Writer writer, Object value) throws IOException {
			writer.append(String.format(Locale.ENGLISH, formatString, value));
		}

	}

	private static PrintFormat findBestPrintFormat(Field field, List<Object> structs) {
		final StructField structInfo = field.getAnnotation(StructField.class);
		final Class<?> type = field.getType().isArray() ? field.getType().getComponentType() : field.getType();
		if (Float.class.equals(type) || float.class.equals(type) || Double.class.equals(type) || double.class.equals(type)) {
			return new PrintFormat("% 5.5f");
		} else if (Byte.class.equals(type) || byte.class.equals(type) || DataType.BIT_8.equals(structInfo.value())
				|| DataType.UBIT_8.equals(structInfo.value())) {
			return new PrintFormat("% 4d");
		} else if (short.class.equals(type) || short.class.equals(type) || DataType.BIT_16.equals(structInfo.value())
				|| DataType.UBIT_16.equals(structInfo.value())) {
			return new PrintFormat("% 4d");
		} else if (Integer.class.equals(type) || int.class.equals(type) || DataType.BIT_32.equals(structInfo.value())
				|| DataType.UBIT_32.equals(structInfo.value()) || DataType.BIT_24.equals(structInfo.value()) || DataType.UBIT_24.equals(structInfo.value())) {
			return new PrintFormat("% 10d");
		} else if (Long.class.equals(type) || Long.class.equals(type)) {
			return new PrintFormat("% 14d");
		}
		// TODO Auto-generated method stub
		return null;
	}

	public static void debug(ModelDataRaw model, Path base) throws NoSuchFieldException, IllegalAccessException {

		final DataTracker bufferReader = getFieldData(model, "bufferReader");

		final Queue<ToDo> waitingResources = new LinkedList<>();

		try {
			final StructM3Header header = getFieldData(model, "header");
			waitingResources.add(new ToDo("header", header));
		} catch (final Throwable e) {
			throw new IllegalStateException(e);
		}

		try {
			while (!waitingResources.isEmpty()) {
				final ToDo resource = waitingResources.poll();
				final Path filePath = base.resolve(resource.getName() + ".txt");

				try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
						StandardOpenOption.TRUNCATE_EXISTING)) {

					final Object struct = resource.getData();
					writer.append(struct.getClass().getSimpleName());
					writer.append("[");
					forEachFieldIn(struct, (field) -> {
						try {
							writer.append(field.getName()).append("=");
							final StructField var = field.getAnnotation(StructField.class);
							if (!var.value().equals(DataType.STRUCT)) {
								printValue(writer, struct, field);
							} else {
								// printStruct(writer, struct, field, fileName, later);
							}
						} catch (final IOException e) {
							throw new IllegalStateException(e);
						}
					});
					writer.append("]");

				}
			}
		} catch (final Throwable e) {
			throw new IllegalStateException(e);
		}

		final String fileName = "header";
		final Path filePath = base.resolve(fileName + ".txt");

		final Writer writer = null;

		final Object struct = null;

		final List<Later> later = new LinkedList<>();

		try {
			writer.append(struct.getClass().getSimpleName());
			writer.append("[");
			forEachFieldIn(struct, (field) -> {
				try {
					writer.append(field.getName()).append("=");
					final StructField var = field.getAnnotation(StructField.class);
					if (!var.value().equals(DataType.STRUCT)) {
						printValue(writer, struct, field);
					} else {
						printStruct(writer, struct, field, fileName, later);
					}
				} catch (final IOException | IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			});
			writer.append("]");

		} catch (final Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	private static void printStruct(final Writer writer, final Object struct, Field field, final String fileName, final List<Later> later)
			throws IllegalAccessException, IOException {
		final boolean isArray = field.getType().isArray();
		final int size = isArray ? Array.getLength(field.get(struct)) : 1;
		final Get getObject = isArray ? new ArrayGet(field.get(struct)) : new FieldGet(field, struct);
		final Class<?> fieldType = isArray ? field.getType().getComponentType() : field.getType();

		if (ArrayTypePointer.class.isAssignableFrom(fieldType) || DoubleArrayTypePointer.class.isAssignableFrom(fieldType)) {
			final String baseLink = fileName + "." + fieldType.getSimpleName();

			if (isArray) {
				writer.append("(").append(String.format("%5d", size)).append(")");
			}
			writer.append("[");
			for (int i = 0; i < size; ++i) {
				if (i != 0) {
					writer.append(", ");
				}
				final String linkTo = baseLink + i;
				writer.append(linkTo).append(".txt");
				later.add(new Later(linkTo, getObject.get()));
			}
			writer.append("]");
		} else {
			writer.append("[");
			for (int i = 0; i < size; ++i) {
				if (i != 0) {
					writer.append(", ");
				}
				writer.append(StructUtil.toString(getObject.get()));
			}
			writer.append("]");
		}
	}

	private static class Later {
		public Later(String name, Object obj) {

		}
	}

	private static interface Get {
		Object get();

		boolean isArray();

		int size();
	}

	private static final class FieldGet implements Get {
		private final Field field;
		private final Object obj;

		public FieldGet(Field field, Object obj) {
			this.field = field;
			this.obj = obj;
		}

		@Override
		public Object get() {
			try {
				return field.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public boolean isArray() {
			return false;
		}

		@Override
		public int size() {
			return 1;
		}
	}

	private static final class SimpleGet implements Get {
		private final Object value;

		public SimpleGet(Object value) {
			this.value = value;
		}

		@Override
		public Object get() {
			return value;
		}

		@Override
		public boolean isArray() {
			return false;
		}

		@Override
		public int size() {
			return 1;
		}
	}

	private static final class ArrayGet implements Get {
		private final Object array;
		private final int size;
		private int index;

		public ArrayGet(Object array) {
			this.array = array;
			this.size = Array.getLength(array);
		}

		@Override
		public Object get() {
			final Object r = Array.get(array, index);
			index += 1;
			if (index == size) {
				index = 0;
			}
			return r;
		}

		@Override
		public boolean isArray() {
			return true;
		}

		@Override
		public int size() {
			return size;
		}
	}

	private static void printValue(final Writer writer, final Object struct, final Field field) {
		try {
			final StructField var = field.getAnnotation(StructField.class);
			final String valueFormat = getValueFormat(field);
			if (var.length() == 1) {
				writer.append(String.format(valueFormat, field.get(struct)));
			} else {
				writer.append("[");
				final Object array = field.get(struct);
				final int arraySize = Array.getLength(array);
				for (int i = 0; i < arraySize; ++i) {
					if (i != 0) {
						writer.append(", ");
					}
					writer.append(String.format(valueFormat, Array.get(array, i)));
				}
				writer.append("]");
			}
		} catch (final IOException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String getValueFormat(Field field) {
		// TODO Auto-generated method stub
		return "% 6d";
	}

}
