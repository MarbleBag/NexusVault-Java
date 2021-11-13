package nexusvault.format.m3.v100.debug;

import java.util.ArrayList;
import java.util.List;

import nexusvault.format.m3.v100.BytePositionTracker;
import nexusvault.format.m3.v100.debug.Table.DataType;
import nexusvault.format.m3.v100.debug.Table.TableCell;
import nexusvault.format.m3.v100.debug.Table.TableColumn;
import nexusvault.format.m3.v100.debug.Table.TableRow;

public final class DoubleDataArrayFormatTask implements Task {
	private final long offsetA;
	private final int sizeOfElementA;
	private final long offsetB;
	private final int sizeOfElementB;
	private final int numberOfElements;
	private final TaskOutput<? super Table> out;

	public DoubleDataArrayFormatTask(long offsetA, int sizeOfElementA, long offsetB, int sizeOfElementB, int numberOfElements, TaskOutput<? super Table> out) {
		this.offsetA = offsetA;
		this.sizeOfElementA = sizeOfElementA;
		this.offsetB = offsetB;
		this.sizeOfElementB = sizeOfElementB;
		this.numberOfElements = numberOfElements;
		this.out = out;
	}

	@Override
	public void runTask(DebugInfo debugger) {
		final Table table = createInitialTable();

		final byte[][] dataA = loadData(debugger.getDataModel(), this.offsetA, this.sizeOfElementA);
		final byte[][] dataB = loadData(debugger.getDataModel(), this.offsetB, this.sizeOfElementB);

		for (int rowIdx = 0; rowIdx < this.numberOfElements; ++rowIdx) {
			final TableRow nextRow = table.addNewRow();

			final byte[] dataARow = dataA[rowIdx];
			final byte[] dataBRow = dataB[rowIdx];

			for (int columnIdx = 0; columnIdx < this.sizeOfElementA; ++columnIdx) {
				final TableColumn column = table.getColumn("A" + columnIdx);
				final TableCell cell = column.getCell(nextRow);
				cell.addEntry(dataARow[columnIdx] & 0xFF);
			}
			for (int columnIdx = 0; columnIdx < this.sizeOfElementB; ++columnIdx) {
				final TableColumn column = table.getColumn("B" + columnIdx);
				final TableCell cell = column.getCell(nextRow);
				cell.addEntry(dataBRow[columnIdx] & 0xFF);
			}
		}
		this.out.setOutput(table);
	}

	private Table createInitialTable() {
		final List<TableColumn> columns = new ArrayList<>(this.sizeOfElementA + this.sizeOfElementB);
		for (int i = 0; i < this.sizeOfElementA; ++i) {
			columns.add(new TableColumn("Block A Column " + i, "A" + i, DataType.UBYTE));
		}
		columns.add(new TableColumn("Padding", "Padding", DataType.NONE));
		for (int i = 0; i < this.sizeOfElementB; ++i) {
			columns.add(new TableColumn("Block B Column " + i, "B" + i, DataType.UBYTE));
		}
		return new Table(columns);
	}

	private byte[][] loadData(final BytePositionTracker data, long offset, int sizeOfElement) {
		data.setPosition(offset);
		final byte[][] loadedData = new byte[this.numberOfElements][sizeOfElement];
		for (final byte[] line : loadedData) {
			data.getData().get(line);
		}
		return loadedData;
	}
}