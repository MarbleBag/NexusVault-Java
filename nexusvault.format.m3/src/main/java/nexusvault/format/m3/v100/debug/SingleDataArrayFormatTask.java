package nexusvault.format.m3.v100.debug;

import java.util.ArrayList;
import java.util.List;

import nexusvault.format.m3.v100.BytePositionTracker;
import nexusvault.format.m3.v100.debug.Table.DataType;
import nexusvault.format.m3.v100.debug.Table.TableCell;
import nexusvault.format.m3.v100.debug.Table.TableColumn;
import nexusvault.format.m3.v100.debug.Table.TableRow;

public final class SingleDataArrayFormatTask implements Task {

	private final long offset;
	private final int sizeOfElement;
	private final int numberOfElements;
	private final TaskOutput<? super Table> out;

	public SingleDataArrayFormatTask(long offset, int sizeOfElement, int numberOfElements, TaskOutput<? super Table> out) {
		this.offset = offset;
		this.sizeOfElement = sizeOfElement;
		this.numberOfElements = numberOfElements;
		this.out = out;
	}

	@Override
	public void runTask(DebugInfo debugger) {
		final Table table = createInitialTable();
		final BytePositionTracker data = debugger.getDataModel();
		data.setPosition(this.offset);

		final byte[][] loadedData = new byte[this.numberOfElements][this.sizeOfElement];

		for (final byte[] line : loadedData) {
			data.getData().get(line);
		}

		for (final byte[] line : loadedData) {
			final TableRow nextRow = table.addNewRow();
			for (int c = 0; c < this.sizeOfElement; ++c) {
				final TableColumn column = table.getColumn(c);
				final TableCell cell = column.getCell(nextRow);
				cell.addEntry(line[c] & 0xFF);
			}
		}
		this.out.setOutput(table);
	}

	private Table createInitialTable() {
		final List<TableColumn> columns = new ArrayList<>(this.sizeOfElement);
		for (int i = 0; i < this.sizeOfElement; ++i) {
			columns.add(new TableColumn("Column " + i, "" + i, DataType.UBYTE));
		}
		return new Table(columns);
	}
}