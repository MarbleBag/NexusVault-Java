package nexusvault.format.m3.v100.debug;

import nexusvault.format.m3.v100.debug.Table.TableCell;

public interface FieldFormater {
	void processField(Util debuger, TableCell cell, FieldReader fieldReader);
}