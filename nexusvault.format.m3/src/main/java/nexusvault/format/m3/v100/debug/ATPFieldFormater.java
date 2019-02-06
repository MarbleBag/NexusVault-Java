package nexusvault.format.m3.v100.debug;

import nexusvault.format.m3.v100.debug.Table.TableCell;
import nexusvault.format.m3.v100.pointer.ArrayTypePointer;

public final class ATPFieldFormater implements FieldFormater {
	@Override
	public void processField(DebugInfo debugger, TableCell cell, FieldReader fieldReader) {
		for (int i = 0; i < fieldReader.size(); ++i) {
			final Object val = fieldReader.get();
			final ArrayTypePointer<?> ptr = (ArrayTypePointer) val;
			if (ptr.hasType()) {
				final TaskOutput<Table> callback = (result) -> cell.addEntry(result);
				debugger.queueTask(new StructFormatTask(ptr.getOffset(), ptr.getTypeOfElement(), ptr.getArraySize(), callback));
			} else {
				final TaskOutput<Table> callback = (result) -> cell.addEntry(result);
				debugger.queueTask(new SingleDataArrayFormatTask(ptr.getOffset(), ptr.getElementSize(), ptr.getArraySize(), callback));
				// cell.addEntry(String.format("ATP [%d->%d[%d]]", ptr.getArraySize(), ptr.getOffset(), ptr.getElementSize()));
			}
		}
	}
}