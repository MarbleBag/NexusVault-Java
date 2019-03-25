package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;

interface PackFile {

	public static final class PackIdxSwap {
		public final long oldPackIdx;
		public final long newPackIdx;

		public PackIdxSwap(long oldPackIdx, long newPackIdx) {
			super();
			this.oldPackIdx = oldPackIdx;
			this.newPackIdx = newPackIdx;
		}
	}

	/**
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws IllegalArgumentException
	 *             if <code>path</code> is null
	 * @throws IllegalStateException
	 *             if another file is already opened
	 */
	void openFile(Path path) throws IOException;

	void closeFile() throws IOException;

	boolean isFileOpen();

	Path getFile();

	void enableWriteMode() throws IOException;

	void flushWrite() throws IOException;

	boolean isWriteModeEnabled();

	BinaryReader getFileReader() throws IOException;

	BinaryWriter getFileWriter() throws IOException;

	/**
	 *
	 * @return
	 * @throws IllegalStateException
	 *             if this instance is not in write mode
	 * @see #enableWriteMode()
	 */
	ArchiveMemoryModel getMemoryModel() throws IllegalStateException;

	/**
	 * Returns the {@link StructPackHeader} for <code>packIdx</code>.
	 *
	 * @return {@link StructPackHeader} associated with <code>packIdx</code>
	 *
	 * @throws IllegalArgumentException
	 *             if <code>packIdx</code> is negative or equal or greater than {@link #getPackArraySize()}
	 */
	StructPackHeader getPack(long packIdx);

	int getPackArrayCapacity();

	/**
	 * @return the number of available {@link StructPackHeader}
	 */
	int getPackArraySize();

	/**
	 * Overwrites {@link StructPackHeader} at <code>packIdx</code>
	 *
	 * @param pack
	 *            will be written at <code>packIdx</code>
	 * @param packIdx
	 *            index at which <code>pack</code> should be written
	 *
	 * @throws IllegalArgumentException
	 *             if <code>pack</code> is null
	 * @throws IllegalArgumentException
	 *             if <code>packIdx</code> is negative or equal or greater than {@link #getPackArraySize()}
	 * @throws IOException
	 *             if an I/O error occurs
	 * 
	 * @see #getPackArraySize()
	 */
	void overwritePack(StructPackHeader pack, long packIdx) throws IOException;

	/**
	 * Writes a new {@link StructPackHeader}. This function may trigger a resizing of the underlying pack array<br>
	 *
	 * @param pack
	 *            to write
	 * @return a new <code>packIdx</code> which will now be associated with the given <code>pack</code>
	 *
	 * @throws IllegalArgumentException
	 *             if <code>pack</code> is null
	 * @throws IOException
	 *             if an I/O error occurs
	 *
	 * @see #setPackArrayAutoGrowSize(int)
	 */
	long writeNewPack(StructPackHeader pack) throws IOException;

	/**
	 * Sets the grow value. In case the {@link #getPackArrayCapacity()} needs to be increased, this value is used to determine how many additional slots needs
	 * to be added.
	 * <p>
	 * The minimal value is 1.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>value</code> is equal or less than 0
	 */
	void setPackArrayAutoGrowSize(int value);

	void setPackArrayCapacityTo(int minimalSize) throws IOException;

	boolean isPackArrayInitialized();

	StructRootBlock getRootElement();

	StructPackHeader writeRootElement(StructRootBlock element) throws IOException;

	/**
	 *
	 * @param packIdx
	 * @return
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws IllegalArgumentException
	 *             if <code>packIdx</code> is negative or equal or greater than {@link #getPackArraySize()}
	 * 
	 * @see #getPackArraySize()
	 */
	// TODO complete javadoc
	PackIdxSwap deletePack(long packIdx) throws IOException;

	void setPackRootIdx(long rootIdx);

	long getPackRootIndex();

}