package nexusvault.format.tbl;

import java.lang.reflect.Array;

public class PreloadedTypedTable<T> extends AbstTypedTable<T> {
	protected final T[] entries;

	@SuppressWarnings("unchecked")
	public PreloadedTypedTable(Class<T> entryClass, RawTable table) {
		super(entryClass);
		this.entries = (T[]) Array.newInstance(entryClass, table.size());
		for (int recordIdx = 0; recordIdx < this.entries.length; ++recordIdx) {
			entries[recordIdx] = buildRecord(table, recordIdx);
		}
	}

	@Override
	public int size() {
		return entries.length;
	}

	@Override
	public T getEntry(int idx) {
		return entries[idx];
	}
}