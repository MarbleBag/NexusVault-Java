package nexusvault.format.tbl;

import java.util.Arrays;
import java.util.Comparator;

public final class TableLookup {
	private TableLookup() {
	}

	/**
	 * Sorts entries according to their ids (first column, low to high) and builds a lookup table based on the sorted array
	 *
	 * @param entries
	 */
	public static int[] sortEntriesAndComputeLookup(Object[][] entries) {

		final Comparator<Object[]> comparator = (a, b) -> (int) a[0] - (int) b[0];
		if (entries.length < 1000) {
			Arrays.sort(entries, comparator);
		} else {
			Arrays.parallelSort(entries, comparator);
		}

		if (entries.length == 0) {
			return new int[0];
		}

		final var maxId = (int) entries[entries.length - 1][0];
		final var lookup = new int[maxId + 1]; // id start with 1

		for (int i = 0, j = 0; i < lookup.length; ++i) {
			lookup[i] = (int) entries[j][0] == i ? j++ : -1;
		}

		return lookup;
	}
}
