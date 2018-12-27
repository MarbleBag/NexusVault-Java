package nexusvault.pack.util;

import java.util.List;

import nexusvault.pack.index.IdxEntry;

public interface IdxCollector<T extends IdxEntry> {
	List<T> getAndClearResult();
}
