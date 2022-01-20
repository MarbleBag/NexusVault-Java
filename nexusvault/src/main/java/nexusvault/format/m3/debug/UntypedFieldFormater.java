package nexusvault.format.m3.debug;

import nexusvault.format.m3.debug.Table.TableCell;

final class UntypedFieldFormater implements FieldFormater {
	@Override
	public void processField(DebugInfo debugger, TableCell cell, FieldReader fieldReader) {
		for (int i = 0; i < fieldReader.size(); ++i) {
			cell.addEntry(fieldReader.get());
		}
	}
}