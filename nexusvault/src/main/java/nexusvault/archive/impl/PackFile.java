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
	 * Returns the {@link StructPackHeader} for <tt>packIdx</tt>. Throws an {@link IndexOutOfBoundsException} if the index is negative or greater than the
	 * {@link #getPackArraySize() array size}
	 *
	 * @param packIdx
	 * @return {@link IndexOutOfBoundsException} associated with <tt>packIdx</tt>
	 * @see #isPackAvailable(long)
	 */
	StructPackHeader getPack(long packIdx);

	int getPackArrayCapacity();

	/**
	 * @return the number of available {@link StructPackHeader}
	 */
	int getPackArraySize();

	/**
	 * @param packIdx
	 * @return true if <tt>packIdx</tt> is valid
	 */
	boolean isPackAvailable(long packIdx);

	/**
	 * Overwrites {@link StructPackHeader} at <tt>packIdx</tt>
	 *
	 * @param pack
	 *            will be written at <tt>packIdx</tt>
	 * @param packIdx
	 *            index at which <tt>pack</tt> should be written
	 *
	 * @throws IllegalArgumentException
	 *             if <tt>pack</tt> is null
	 * @throws IndexOutOfBoundsException
	 *             if <tt>packIdx</tt> is invalid
	 * @throws IOException
	 *             if an I/O error occurs
	 *
	 * @see #isPackAvailable(long)
	 */
	void overwritePack(StructPackHeader pack, long packIdx) throws IOException;

	/**
	 * Writes a new {@link StructPackHeader}. This function may trigger a resizing of the underlying pack array<br>
	 *
	 * @param pack
	 *            to write
	 * @return a new <tt>packIdx</tt> which will now be associated with the given <tt>pack</tt>
	 *
	 * @throws IllegalArgumentException
	 *             if <tt>pack</tt> is null
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
	 *             if <tt>value</tt> is equal or less than 0
	 */
	void setPackArrayAutoGrowSize(int value);

	void setPackArrayCapacityTo(int minimalSize) throws IOException;

	boolean isPackArrayInitialized();

	StructRootBlock getRootElement();

	StructPackHeader writeRootElement(StructRootBlock element) throws IOException;

	PackIdxSwap deletePack(long packIdx) throws IOException;

	void setPackRootIdx(long rootIdx);

	long getPackRootIndex();

}