package nexusvault.format.m3.v100.debug;

import nexusvault.format.m3.v100.debug.Table.TableCell;
import nexusvault.format.m3.v100.pointer.DoubleArrayTypePointer;

public final class DATPFieldFormater implements FieldFormater {
	@Override
	public void processField(Util debuger, TableCell cell, FieldReader fieldReader) {
		for (int i = 0; i < fieldReader.size(); ++i) {
			final Object val = fieldReader.get();
			final DoubleArrayTypePointer<?, ?> ptr = (DoubleArrayTypePointer) val;
			cell.addEntry(String.format("DATP [%d->%d[%d], %d[%d]]", ptr.getArraySize(), ptr.getOffsetA(), ptr.getElementSizeA(), ptr.getOffsetB(),
					ptr.getElementSizeB()));
		}
	}
}