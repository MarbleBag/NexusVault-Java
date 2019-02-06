package nexusvault.archive.util;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxFileLink;

public interface IdxEntryVisitor {
	public enum EntryFilterResult {
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
	 * Called after <i>preVisitDirectory</i>, but before <i>postVisitDirectory</i> for each file that is contained in the directory. The files are not traversed
	 * in any particular order.
	 *
	 * @param file
	 *            : the visited file
	 * @return How to proceed after this node
	 */
	EntryFilterResult visitFile(IdxFileLink file);
}