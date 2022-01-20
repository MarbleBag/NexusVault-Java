package nexusvault.format.m3.debug;

import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.format.m3.debug.Table.TableCell;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.pointer.ArrayTypePointer;
import nexusvault.shared.Text;

public final class NullTerminatedStringFieldFormater implements FieldFormater {
	@Override
	public void processField(DebugInfo debugger, TableCell cell, FieldReader fieldReader) {
		for (int i = 0; i < fieldReader.size(); ++i) {
			final Object val = fieldReader.get();
			final ArrayTypePointer<?> ptr = (ArrayTypePointer) val;
			if (ptr.hasType()) {
				cell.addEntry(String.format("Error: String formater is not for typed pointer"));
			} else {
				if (ptr.getOffset() == 0) {
					continue;
				}

				final BytePositionTracker data = debugger.getDataModel();
				data.setPosition(ptr.getOffset());

				final String txt = Text.readNullTerminatedUTF16(new ByteBufferBinaryReader(data.getData()));
				cell.addEntry(txt);
			}
		}
	}
}