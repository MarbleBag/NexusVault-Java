package nexusvault.pack.util;

import java.util.Deque;
import java.util.LinkedList;

import nexusvault.pack.index.IdxDirectory;
import nexusvault.pack.index.IdxFileLink;
import nexusvault.pack.util.IdxEntryVisitor.EntryFilterResult;

public class IdxDirectoryTraverser {

	public static <T extends IdxEntryVisitor> T visitEntries(IdxDirectory start, T visitor) {
		if (start == null) {
			throw new IllegalArgumentException("'start' must not be null");
		}
		if (visitor == null) {
			throw new IllegalArgumentException("'visitor' must not be null");
		}

		final Deque<IdxDirectory> unvisited = new LinkedList<>();
		unvisited.add(start);

		while (!unvisited.isEmpty()) {
			final IdxDirectory dir = unvisited.pollFirst();

			final EntryFilterResult preVisitResult = visitor.preVisitDirectory(dir);
			if (EntryFilterResult.TERMINATE == preVisitResult) {
				return visitor;
			}

			if (EntryFilterResult.SKIP_SUBTREE == preVisitResult) {
				continue;
			}

			boolean skipDirs = EntryFilterResult.SKIP_DIRECTORIES == preVisitResult;

			if (EntryFilterResult.SKIP_FILES != preVisitResult) {
				for (final IdxFileLink child : dir.getFiles()) {
					final EntryFilterResult fileResult = visitor.visitFile(child);

					if (EntryFilterResult.TERMINATE == fileResult) {
						return visitor;
					} else if (EntryFilterResult.SKIP_FILES == fileResult) {
						break;
					} else if (EntryFilterResult.SKIP_SUBTREE == fileResult) {
						skipDirs = true;
						break;
					} else if (EntryFilterResult.SKIP_DIRECTORIES == fileResult) {
						skipDirs = true;
					}
				}
			}

			final EntryFilterResult postVisitResult = visitor.postVisitDirectory(dir);
			if (EntryFilterResult.TERMINATE == postVisitResult) {
				return visitor;
			} else if (EntryFilterResult.SKIP_SUBTREE == postVisitResult) {
				skipDirs = true;
			} else if (EntryFilterResult.SKIP_DIRECTORIES == postVisitResult) {
				skipDirs = true;
			}

			if (!skipDirs) {
				for (final IdxDirectory child : dir.getSubDirectories()) {
					unvisited.addLast(child);
				}
			}
		}

		return visitor;
	}

}
