package nexusvault.archive.util;

import java.util.function.Predicate;

import nexusvault.archive.IdxFileLink;

public class ReportingIdxFileCollector extends IdxFileCollector {

	public static interface ReportListener {

		void visitedFile(IdxFileLink file, boolean predicateTest, EntryFilterResult visitorResult);

	}

	private ReportListener listener;

	public ReportingIdxFileCollector(Predicate<IdxFileLink> predicate) {
		this(predicate, -1);
	}

	public ReportingIdxFileCollector(Predicate<IdxFileLink> predicate, int maxNumberOfResults) {
		super(predicate, maxNumberOfResults);
	}

	public void setListener(ReportListener listener) {
		this.listener = listener;
	}

	public ReportListener getListener() {
		return this.listener;
	}

	@Override
	public EntryFilterResult visitFile(IdxFileLink file) {
		EntryFilterResult visitorResult = EntryFilterResult.CONTINUE;
		final boolean predicateTest = predicate.test(file);
		if (predicateTest) {
			result.add(file);
			if ((0 <= maxNumberOfResults) && (maxNumberOfResults <= result.size())) {
				visitorResult = EntryFilterResult.TERMINATE;
			}
		}

		if (listener != null) {
			listener.visitedFile(file, predicateTest, visitorResult);
		}

		return visitorResult;
	}

}
