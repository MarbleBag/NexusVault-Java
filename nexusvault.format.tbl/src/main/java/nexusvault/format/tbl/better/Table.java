package nexusvault.format.tbl.better;

import java.util.Iterator;

public interface Table<T> extends Iterable<T> {

	String getName();

	int size();

	T getByIndex(int index);

	T getById(int id);

	T remove(int index);

	void add(T entry);

	void add(T entry, int index);

	@Override
	default Iterator<T> iterator() {
		return new Iterator<>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < size();
			}

			@Override
			public T next() {
				return getByIndex(idx++);
			}
		};
	}
}
