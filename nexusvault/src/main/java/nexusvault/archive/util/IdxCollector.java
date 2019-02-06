package nexusvault.archive.util;

import java.util.List;

import nexusvault.archive.IdxEntry;

public interface IdxCollector<T extends IdxEntry> {
	List<T> getAndClearResult();
}
