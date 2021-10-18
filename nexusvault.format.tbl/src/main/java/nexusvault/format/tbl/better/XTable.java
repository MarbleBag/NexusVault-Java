package nexusvault.format.tbl.better;

import java.util.Iterator;
import java.util.List;

public class XTable<T> implements Table<T> {
	public String name;
	public Column[] columns;
	public List<T> entries;
	public int[] lookup;
	public long unk1;

	public XTable(String name, Column[] columns, List<T> entries, int[] lookup) {
		this.name = name;
		this.columns = columns;
		this.entries = entries;
		this.lookup = lookup;
	}

	@Override
	public T getById(int id) {
		final var idx = this.lookup[id];
		if (idx == -1) {
			return null;
		}
		return this.entries.get(idx);
	}

	@Override
	public T getByIndex(int index) {
		return this.entries.get(index);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int size() {
		return this.entries.size();
	}

	@Override
	public T remove(int index) {
		// TODO update lookup!
		return null;
	}

	@Override
	public void add(T entry) {
		// TODO update lookup
	}

	@Override
	public void add(T entry, int index) {
		// TODO update lookup
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < size();
			}

			@Override
			public T next() {
				return getByIndex(this.idx++);
			}
		};
	}
}
