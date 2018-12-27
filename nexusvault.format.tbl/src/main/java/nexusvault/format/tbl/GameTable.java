package nexusvault.format.tbl;

import java.util.Iterator;

public interface GameTable<T> extends Iterable<T> {

	int size();

	T getEntry(int idx);

	@Override
	default Iterator<T> iterator() {
		return new Iterator<T>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < size();
			}

			@Override
			public T next() {
				return getEntry(idx++);
			}
		};
	}

}
