package nexusvault.vault.util;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import nexusvault.vault.IdxEntry.IdxDirectory;
import nexusvault.vault.IdxEntry.IdxFileLink;
import nexusvault.vault.util.IdxDirectoryTraverser.IdxEntryVisitor.EntryFilterResult;

/**
 * Traverses a given file structure, calling for each {@link IdxDirectory directory} or {@link IdxFileLink file} it encounters the respective method on the
 * given {@link IdxEntryVisitor visitor} and acts accordingly to its return {@link EntryFilterResult value}.
 *
 * @see EntryFilterResult
 * @see IdxEntryVisitor
 */
public final class IdxDirectoryTraverser {

	public static interface IdxEntryVisitor {
		public static enum EntryFilterResult {
			/** proceed */
			CONTINUE,
			/** terminates the traversal immediately */
			TERMINATE,
			/** Skip all sub directories and files which follow this node */
			SKIP_SUBTREE,
			/** Skip all files which follow this node */
			SKIP_FILES,
			/** Skip all sub directories which follow this node */
			SKIP_DIRECTORIES
		}

		/**
		 * Called before the given directory is traversed.
		 *
		 * @param nextDir
		 *            : the next directory to travers
		 * @return How to proceed after this node. By default this method returns <i>CONTINUE</i>.
		 */
		default EntryFilterResult preVisitDirectory(IdxDirectory nextDir) {
			return EntryFilterResult.CONTINUE;
		}

		/**
		 * Called after the given directory was traversed. This method is not called if any method before terminates the traversal.
		 *
		 * @param lastDir
		 *            : the directory that was traversed
		 * @return How to proceed after this node. By default this method returns <i>CONTINUE</i>.
		 */
		default EntryFilterResult postVisitDirectory(IdxDirectory lastDir) {
			return EntryFilterResult.CONTINUE;
		}

		/**
		 * Called after <i>preVisitDirectory</i>, but before <i>postVisitDirectory</i> for each file that is contained in the directory. The files are not
		 * traversed in any particular order.
		 *
		 * @param file
		 *            : the visited file
		 * @return How to proceed after this node
		 */
		EntryFilterResult visitFile(IdxFileLink file);
	}

	public static <T extends IdxEntryVisitor> T visitEntries(IdxDirectory start, T visitor) throws IOException {
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
				for (final IdxDirectory child : dir.getDirectories()) {
					unvisited.addLast(child);
				}
			}
		}

		return visitor;
	}

}
