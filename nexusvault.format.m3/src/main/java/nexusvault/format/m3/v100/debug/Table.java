package nexusvault.format.m3.v100.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Table {

	public static final class TableCell {
		private final TableColumn column;
		private final TableRow row;

		public TableCell(TableRow row, TableColumn column) {
			this.row = row;
			this.column = column;
		}

		public void addEntry(Object value) {
			row.addEntry(column, value);
		}

		public void addEntries(Collection<? extends Object> value) {
			row.addEntries(column, value);
		}

		public TableColumn getColumn() {
			return column;
		}

		public List<Object> getEntries() {
			return row.getEntries(column);
		}

		public TableRow getRow() {
			return row;
		}

	}

	public static final class TableColumn {
		private final String columnId;
		private final String columnName;
		private int index;

		private int maxNumberOfEntries = 1;
		private final List<String> subHeader;

		public TableColumn(String columnName) {
			this(columnName, null);
		}

		public TableColumn(String columnName, String columnId) {
			if (columnName == null) {
				throw new IllegalArgumentException("'columnName' must not be null");
			}
			this.columnName = columnName;
			this.columnId = columnId == null ? "" : columnId;

			subHeader = new ArrayList<>(0);
		}

		public void addSubHeader(String subColumn) {
			if (subColumn == null) {
				throw new IllegalArgumentException("'subHeader' must not be null");
			}
			subHeader.add(subColumn);
		}

		public TableCell getCell(TableRow row) {
			return new TableCell(row, this);
		}

		public String getColumnName() {
			return columnName;
		}

		public String getId() {
			return columnId;
		}

		public int getIndex() {
			return index;
		}

		public int getMaxNumberOfEntries() {
			return maxNumberOfEntries;
		}

		public List<String> getSubHeader() {
			return Collections.unmodifiableList(subHeader);
		}

		public void setArrayAnnotation(int size) {
			maxNumberOfEntries = size;
		}

		void updateIndex(int index) {
			this.index = index;
		}

		TableColumn cloneColumn() {
			final TableColumn clone = new TableColumn(columnName, columnId);
			clone.subHeader.addAll(subHeader);
			clone.maxNumberOfEntries = maxNumberOfEntries;
			clone.index = index;
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
			return index;
		}

		void addEntry(TableColumn column, Object value) {
			if (row[column.getIndex()] == null) {
				row[column.getIndex()] = new LinkedList<>();
			} else {
				if (row[column.getIndex()].size() >= column.getMaxNumberOfEntries()) {
					throw new IllegalArgumentException("Column " + column.getId() + " can only have up to " + column.getMaxNumberOfEntries());
				}
			}
			row[column.getIndex()].add(value);
		}

		public void addEntries(TableColumn column, Collection<? extends Object> value) {
			if (row[column.getIndex()] == null) {
				row[column.getIndex()] = new LinkedList<>();
			} else {
				if ((row[column.getIndex()].size() + value.size()) > column.getMaxNumberOfEntries()) {
					throw new IllegalArgumentException("Column " + column.getId() + " can only have up to " + column.getMaxNumberOfEntries());
				}
			}
			row[column.getIndex()].addAll(value);
		}

		List<Object> getEntries(TableColumn column) {
			if (row[column.getIndex()] == null) {
				return Collections.emptyList();
			}
			return Collections.unmodifiableList(row[column.getIndex()]);
		}
	}

	private final List<List<Object>[]> model;

	// Map<String, TableColumn> columns;
	private final List<TableColumn> sortedColumns;

	public Table(List<TableColumn> columns) {
		// columns = new HashMap<>();
		sortedColumns = new ArrayList<>();

		for (final TableColumn column : columns) {
			column.updateIndex(sortedColumns.size());
			sortedColumns.add(column);
		}

		model = new ArrayList<>(24);
	}

	@SuppressWarnings("unchecked")
	public TableRow addNewRow() {
		final List<Object>[] row = new List[sortedColumns.size()];
		final int index = model.size();
		model.add(row);
		return new TableRow(index, row);
	}

	public TableCell getCell(TableRow row, TableColumn column) {
		return column.getCell(row);
	}

	public TableColumn getColumn(int index) {
		return sortedColumns.get(index);
	}

	public TableColumn getColumn(String columnId) {
		for (final TableColumn column : sortedColumns) {
			if (column.getId().equals(columnId)) {
				return column;
			}
		}
		return null;
	}

	public int getColumnCount() {
		return sortedColumns.size();
	}

	public TableRow getRow(int index) {
		if ((index < 0) || (model.size() <= index)) {
			throw new IndexOutOfBoundsException(String.format("Index out of bound. Allowed range [0,%)", model.size()));
		}
		return new TableRow(index, model.get(index));
	}

	public int getRowCount() {
		return model.size();
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