package nexusvault.archive.util;

import java.util.List;

import nexusvault.archive.IdxEntry;

public interface IdxCollector<T extends IdxEntry> {
	/**
	 * Returns the results and clears the internal memory. <br>
	 * Subsequent calls to this method will return an empty list.
	 */
	List<T> getAndClearResult();
}
