package nexusvault.format.m3.debug;

import nexusvault.format.m3.debug.Table.TableCell;

public interface FieldFormater {
	void processField(DebugInfo debugger, TableCell cell, FieldReader fieldReader);
}