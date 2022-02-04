/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.vault.index;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import nexusvault.vault.IdxPath;
import nexusvault.vault.index.IndexException.IndexNameCollisionException;

public interface Node {

	public static interface DirectoryNode extends Node {
		DirectoryNode newDirectory(String name) throws IndexNameCollisionException, IOException;

		FileNode newFile(String name, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash, int unk_034)
				throws IndexNameCollisionException, IOException;

		void delete(String name) throws IOException;

		boolean hasChild(String name) throws IOException;

		Optional<Node> getChild(String name) throws IOException;

		List<Node> getChilds() throws IOException;

		List<DirectoryNode> getDirectories() throws IOException;

		List<FileNode> getFiles() throws IOException;

		Optional<Node> find(IdxPath path) throws IOException;

		Node findLast(IdxPath path) throws IOException;

		int countNodesInSubTree() throws IOException;
	}

	public static interface FileNode extends Node {
		int getFlags();

		long getWriteTime();

		long getUncompressedSize();

		long getCompressedSize();

		byte[] getHash();

		int getUnk_034();

		/**
		 * Sets only non-null values
		 *
		 * @param name
		 *            file name
		 * @param flags
		 *            file specific flags
		 * @param writeTime
		 *            time of creation / modification
		 * @param uncompressedSize
		 *            uncompressed file size
		 * @param compressedSize
		 *            compressed file size
		 * @param hash
		 *            SHA-1 hash of file
		 * @param unk_034
		 *            unknown
		 */
		void overwrite(String name, Integer flags, Long writeTime, Long uncompressedSize, Long compressedSize, byte[] hash, Integer unk_034);

		void setFlags(int flags);

		void setWriteTime(long writeTime);

		void setUncompressedSize(long uncompressedSize);

		void setCompressedSize(long compressedSize);

		void setHash(byte[] hash);

		void setUnk_034(int unk_034);
	}

	String getName();

	void setName(String name);

	DirectoryNode getParent();

	boolean isDirectory();

	boolean isFile();

	FileNode asFile();

	DirectoryNode asDirectory();

	void moveTo(DirectoryNode parent) throws IOException;

	IdxPath toPath();
}
