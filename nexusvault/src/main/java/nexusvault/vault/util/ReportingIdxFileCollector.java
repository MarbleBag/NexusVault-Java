package nexusvault.vault.util;

import java.util.function.Predicate;

import nexusvault.vault.IdxEntry.IdxFileLink;

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
		final boolean predicateTest = this.predicate.test(file);
		if (predicateTest) {
			this.result.add(file);
			if (0 <= this.maxNumberOfResults && this.maxNumberOfResults <= this.result.size()) {
				visitorResult = EntryFilterResult.TERMINATE;
			}
		}

		if (this.listener != null) {
			this.listener.visitedFile(file, predicateTest, visitorResult);
		}

		return visitorResult;
	}

}
