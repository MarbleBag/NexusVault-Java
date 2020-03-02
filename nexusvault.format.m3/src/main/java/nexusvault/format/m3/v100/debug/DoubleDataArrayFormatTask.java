package nexusvault.format.m3.v100.debug;

import java.util.ArrayList;
import java.util.List;

import nexusvault.format.m3.v100.BytePositionTracker;
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

		final byte[][] dataA = loadData(debugger.getDataModel(), offsetA, sizeOfElementA);
		final byte[][] dataB = loadData(debugger.getDataModel(), offsetB, sizeOfElementB);

		for (int rowIdx = 0; rowIdx < numberOfElements; ++rowIdx) {
			final TableRow nextRow = table.addNewRow();

			final byte[] dataARow = dataA[rowIdx];
			final byte[] dataBRow = dataB[rowIdx];

			for (int columnIdx = 0; columnIdx < sizeOfElementA; ++columnIdx) {
				final TableColumn column = table.getColumn("A" + columnIdx);
				final TableCell cell = column.getCell(nextRow);
				cell.addEntry((dataARow[columnIdx] & 0xFF));
			}
			for (int columnIdx = 0; columnIdx < sizeOfElementB; ++columnIdx) {
				final TableColumn column = table.getColumn("B" + columnIdx);
				final TableCell cell = column.getCell(nextRow);
				cell.addEntry((dataBRow[columnIdx] & 0xFF));
			}
		}
		out.setOutput(table);
	}

	private Table createInitialTable() {
		final List<TableColumn> columns = new ArrayList<>(sizeOfElementA + sizeOfElementB);
		for (int i = 0; i < sizeOfElementA; ++i) {
			columns.add(new TableColumn("Block A Column " + i, "A" + i));
		}
		columns.add(new TableColumn("Padding", "Padding"));
		for (int i = 0; i < sizeOfElementB; ++i) {
			columns.add(new TableColumn("Block B Column " + i, "B" + i));
		}
		return new Table(columns);
	}

	private byte[][] loadData(final BytePositionTracker data, long offset, int sizeOfElement) {
		data.setPosition(offset);
		final byte[][] loadedData = new byte[numberOfElements][sizeOfElement];
		for (final byte[] line : loadedData) {
			data.getData().get(line);
		}
		return loadedData;
	}
}