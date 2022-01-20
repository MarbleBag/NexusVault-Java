package nexusvault.format.m3.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class Table implements Iterable<Table.TableRow> {

	public static final class TableCell {
		private final TableColumn column;
		private final TableRow row;

		public TableCell(TableRow row, TableColumn column) {
			this.row = row;
			this.column = column;
		}

		public void addEntry(Object value) {
			this.row.addEntry(this.column, value);
		}

		public void addEntries(Collection<? extends Object> value) {
			this.row.addEntries(this.column, value);
		}

		public TableColumn getColumn() {
			return this.column;
		}

		public List<Object> getEntries() {
			return this.row.getEntries(this.column);
		}

		public TableRow getRow() {
			return this.row;
		}

	}

	public static enum DataType {
		NONE,
		UBYTE,
		BYTE,
		SHORT,
		USHORT,
		INT,
		UINT,
		LONG,
		ULONG,
		HFLOAT,
		FLOAT,
		DOUBLE,
		STRUCT;

		protected static DataType resolveType(Class<?> fieldType, kreed.reflection.struct.DataType structType) {
			switch (structType) {
				case BIT_8:
					return BYTE;
				case UBIT_8:
					return UBYTE;
				case UBIT_16:
					return USHORT;
				case BIT_16:
					if (isFloat(fieldType) || isDouble(fieldType)) {
						return HFLOAT;
					}
					return SHORT;
				case BIT_24:
				case BIT_32:
					if (isFloat(fieldType) || isDouble(fieldType)) {
						return FLOAT;
					}
					return INT;
				case UBIT_24:
				case UBIT_32:
					return UINT;
				case BIT_64:
					if (isDouble(fieldType)) {
						return DOUBLE;
					}
					return LONG;
				case UBIT_64:
					return ULONG;
				case STRUCT:
					return DataType.STRUCT;
				default:
					return NONE;
			}
		}

		private static boolean isByte(Class<?> type) {
			if (type.isArray()) {
				return isByte(type.getComponentType());
			}
			return byte.class.equals(type) || Byte.class.equals(type);
		}

		private static boolean isShort(Class<?> type) {
			if (type.isArray()) {
				return isShort(type.getComponentType());
			}
			return short.class.equals(type) || Short.class.equals(type);
		}

		private static boolean isInt(Class<?> type) {
			if (type.isArray()) {
				return isInt(type.getComponentType());
			}
			return int.class.equals(type) || Integer.class.equals(type);
		}

		private static boolean isLong(Class<?> type) {
			if (type.isArray()) {
				return isLong(type.getComponentType());
			}
			return long.class.equals(type) || Long.class.equals(type);
		}

		private static boolean isDouble(Class<?> type) {
			if (type.isArray()) {
				return isDouble(type.getComponentType());
			}
			return double.class.equals(type) || Double.class.equals(type);
		}

		private static boolean isFloat(Class<?> type) {
			if (type.isArray()) {
				return isFloat(type.getComponentType());
			}
			return float.class.equals(type) || Float.class.equals(type);
		}
	}

	public static final class TableColumn {
		private final String columnId;
		private final String columnName;
		private final DataType dataType;
		private int index;

		private int maxNumberOfEntries = 1;
		private final List<String> subHeader;

		public TableColumn(String columnName, DataType dataType) {
			this(columnName, null, dataType);
		}

		public TableColumn(String columnName, String columnId, DataType dataType) {
			if (columnName == null) {
				throw new IllegalArgumentException("'columnName' must not be null");
			}
			this.columnName = columnName;
			this.columnId = columnId == null ? "" : columnId;
			this.dataType = dataType;

			this.subHeader = new ArrayList<>(0);
		}

		public void addSubHeader(String subColumn) {
			if (subColumn == null) {
				throw new IllegalArgumentException("'subHeader' must not be null");
			}
			this.subHeader.add(subColumn);
		}

		public TableCell getCell(TableRow row) {
			return new TableCell(row, this);
		}

		public String getColumnName() {
			return this.columnName;
		}

		public String getId() {
			return this.columnId;
		}

		public int getIndex() {
			return this.index;
		}

		public DataType getDataType() {
			return this.dataType;
		}

		public int getMaxNumberOfEntries() {
			return this.maxNumberOfEntries;
		}

		public List<String> getSubHeader() {
			return Collections.unmodifiableList(this.subHeader);
		}

		public void setArrayAnnotation(int size) {
			this.maxNumberOfEntries = size;
		}

		void updateIndex(int index) {
			this.index = index;
		}

		TableColumn cloneColumn() {
			final TableColumn clone = new TableColumn(this.columnName, this.columnId, this.dataType);
			clone.subHeader.addAll(this.subHeader);
			clone.maxNumberOfEntries = this.maxNumberOfEntries;
			clone.index = this.index;
			return clone;
		}

	}

	public static final class TableRow {
		private final int index;
		private final List<Object>[] row;

		private TableRow(int index, List<Object>[] row) {
			this.index = index;
			this.row = row;
		}

		public TableCell getCell(TableColumn column) {
			return column.getCell(this);
		}

		public int getIndex() {
			return this.index;
		}

		void addEntry(TableColumn column, Object value) {
			if (this.row[column.getIndex()] == null) {
				this.row[column.getIndex()] = new LinkedList<>();
			} else {
				if (this.row[column.getIndex()].size() >= column.getMaxNumberOfEntries()) {
					throw new IllegalArgumentException("Column " + column.getId() + " can only have up to " + column.getMaxNumberOfEntries());
				}
			}
			this.row[column.getIndex()].add(value);
		}

		public void addEntries(TableColumn column, Collection<? extends Object> value) {
			if (this.row[column.getIndex()] == null) {
				this.row[column.getIndex()] = new LinkedList<>();
			} else {
				if (this.row[column.getIndex()].size() + value.size() > column.getMaxNumberOfEntries()) {
					throw new IllegalArgumentException("Column " + column.getId() + " can only have up to " + column.getMaxNumberOfEntries());
				}
			}
			this.row[column.getIndex()].addAll(value);
		}

		List<Object> getEntries(TableColumn column) {
			if (this.row[column.getIndex()] == null) {
				return Collections.emptyList();
			}
			return Collections.unmodifiableList(this.row[column.getIndex()]);
		}
	}

	private final List<List<Object>[]> model;

	// Map<String, TableColumn> columns;
	private final List<TableColumn> sortedColumns;

	public Table(List<TableColumn> columns) {
		// columns = new HashMap<>();
		this.sortedColumns = new ArrayList<>();

		for (final TableColumn column : columns) {
			column.updateIndex(this.sortedColumns.size());
			this.sortedColumns.add(column);
		}

		this.model = new ArrayList<>(24);
	}

	@SuppressWarnings("unchecked")
	public TableRow addNewRow() {
		final List<Object>[] row = new List[this.sortedColumns.size()];
		final int index = this.model.size();
		this.model.add(row);
		return new TableRow(index, row);
	}

	public TableCell getCell(TableRow row, TableColumn column) {
		return column.getCell(row);
	}

	public TableColumn getColumn(int index) {
		return this.sortedColumns.get(index);
	}

	public TableColumn getColumn(String columnId) {
		for (final TableColumn column : this.sortedColumns) {
			if (column.getId().equals(columnId)) {
				return column;
			}
		}
		return null;
	}

	public int getColumnCount() {
		return this.sortedColumns.size();
	}

	public TableRow getRow(int index) {
		if (index < 0 || this.model.size() <= index) {
			throw new IndexOutOfBoundsException(String.format("Index out of bound. Allowed range [0,%)", this.model.size()));
		}
		return new TableRow(index, this.model.get(index));
	}

	@Override
	public Iterator<TableRow> iterator() {
		final var iterator = this.model.iterator();
		return new Iterator<>() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public TableRow next() {
				return new TableRow(this.idx++, iterator.next());
			}
		};
	}

	public int getRowCount() {
		return this.model.size();
	}

	@Override
	public String toString() {
		return "Table [" + super.hashCode() + ", columns=" + getColumnCount() + ", rows=" + getRowCount() + "]";
	}

	public static Table mergeColumns(Table a, Table b) {
		if (a.getRowCount() != b.getRowCount()) {
			throw new IllegalArgumentException("Tables need to have an equal row count");
		}

		final List<TableColumn> newColumns = new ArrayList<>(a.getColumnCount() + b.getColumnCount());
		a.sortedColumns.stream().map(TableColumn::cloneColumn).forEach(newColumns::add);
		b.sortedColumns.stream().map(TableColumn::cloneColumn).forEach(newColumns::add);
		final Table newTable = new Table(newColumns);

		final int rowCount = a.getRowCount();
		final int columnCountA = a.getColumnCount();
		final int columnCountB = b.getColumnCount();

		for (int i = 0; i < rowCount; ++i) {
			final TableRow row = newTable.addNewRow();
			final TableRow rowA = a.getRow(i);
			final TableRow rowB = b.getRow(i);
			for (int j = 0; j < columnCountA; ++j) {
				final TableColumn column = newTable.getColumn(j);
				final TableColumn columnA = a.getColumn(j);
				row.getCell(column).addEntries(rowA.getCell(columnA).getEntries());
			}
			for (int j = 0; j < columnCountB; ++j) {
				final TableColumn column = newTable.getColumn(columnCountA + j);
				final TableColumn columnB = b.getColumn(j);
				row.getCell(column).addEntries(rowB.getCell(columnB).getEntries());
			}
		}

		return newTable;
	}

}