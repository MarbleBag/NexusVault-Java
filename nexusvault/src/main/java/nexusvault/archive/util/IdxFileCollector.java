package nexusvault.archive.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import nexusvault.archive.IdxFileLink;

/**
 * A utility class that provides a mean to collect a number of {@link IdxFileLink} for a given {@link Predicate predicate}
 * <p>
 * If a {@link IdxFileLink} fulfills a {@link Predicate predicate}, the file will be added to the result, which can be retrieved with
 * {@link #getAndClearResult()}
 * <p>
 * This collector implements {@link IdxEntryVisitor} interface and thus can be used in combination with {@link IdxDirectoryTraverser}
 *
 * @see ReportingIdxFileCollector
 */
public class IdxFileCollector implements IdxEntryVisitor, IdxCollector<IdxFileLink> {

	protected final int maxNumberOfResults;
	protected final List<IdxFileLink> result;
	protected final Predicate<IdxFileLink> predicate;

	public IdxFileCollector(Predicate<IdxFileLink> predicate) {
		this(predicate, -1);
	}

	public IdxFileCollector(Predicate<IdxFileLink> predicate, int maxNumberOfResults) {
		if (predicate == null) {
			throw new IllegalArgumentException("'predicate' must not be null");
		}
		this.predicate = predicate;
		this.result = new LinkedList<>();
		this.maxNumberOfResults = maxNumberOfResults;
	}

	@Override
	public EntryFilterResult visitFile(IdxFileLink file) {
		if (predicate.test(file)) {
			result.add(file);
			if ((0 <= maxNumberOfResults) && (maxNumberOfResults <= result.size())) {
				return EntryFilterResult.TERMINATE;
			}
		}
		return EntryFilterResult.CONTINUE;
	}

	@Override
	public List<IdxFileLink> getAndClearResult() {
		final List<IdxFileLink> result = new ArrayList<>(this.result);
		this.result.clear();
		return result;
	}

}
