package nexusvault.archive.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import nexusvault.shared.exception.IntegerOverflowException;

/**
 * An abstract model of how the memory inside of .index- and .archive-files is organized. Its used to ease the task to the allocate new, reuse old and free
 * space within the files.
 */
final class ArchiveMemoryModel {

	public final class MemoryBlock implements Comparable<MemoryBlock> {
		private final long position;
		private int size;
		private boolean free;

		private MemoryBlock(long position, int size, boolean free) {
			this.position = position;
			this.size = size;
			this.free = free;
		}

		public void clearUpdateFlag() {
			pendingUpdate.remove(this);
		}

		@Override
		public int compareTo(MemoryBlock o) {
			final long diff = position - o.position;
			return diff < 0 ? -1 : diff > 0 ? 1 : 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final MemoryBlock other = (MemoryBlock) obj;
			if (position != other.position) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + (int) (position ^ (position >>> 32));
			return result;
		}

		public boolean isFree() {
			return free;
		}

		public long position() {
			return position;
		}

		public int size() {
			return size;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("MemoryBlock [position=");
			builder.append(position);
			builder.append(", size=");
			builder.append(size);
			builder.append(", free=");
			builder.append(free);
			builder.append("]");
			return builder.toString();
		}
	}

	private static final long SPLIT_THRESHOLD = alignValue(6 * Long.BYTES);

	private static long alignValue(long pos) {
		return (pos + 0xF) & 0xFFFFFFFFFFFFFFF0l;
	}

	private final SortedSet<MemoryBlock> unusedBlocks = new TreeSet<>();
	private final SortedSet<MemoryBlock> usedBlocks = new TreeSet<>();
	private final SortedSet<MemoryBlock> pendingUpdate = new TreeSet<>();
	private final long memOffset;

	public ArchiveMemoryModel(long memOffset) {
		this.memOffset = alignValue(memOffset);
	}

	/**
	 * Allocates a memory block which is equal or bigger than the <code>requestedSize</code>.<br>
	 * The returned {@link MemoryBlock} may either be a block which is marked as <b>free</b> or, in case no such block with the requested size is available, a
	 * newly created block. <br>
	 * <p>
	 * The returned block will be marked as <b>not free</b> and will never be returned by this method again, until marked as <b>free</b> again by calling
	 * {@link #freeMemory(MemoryBlock)}
	 * <p>
	 * A call to this method will result in {@link #getMemoryToUpdate() new pending updates}.
	 *
	 * @param requestedSize
	 *            is the minimal size of the requested block
	 * @return A {@link MemoryBlock} which size is equal or greater than the requested size.
	 * @see MemoryBlock
	 * @see #allocateNewMemory(long)
	 * @see #freeMemory(MemoryBlock)
	 * @see #getMemoryToUpdate()
	 */
	// TODO Split blocks which are 'far' bigger than needed
	public MemoryBlock allocateMemory(long requestedSize) {
		requestedSize = alignValue(requestedSize);

		MemoryBlock unusedBlock = findUnusedBlock(requestedSize);
		if (unusedBlock != null) {
			if (unusedBlock.size > (requestedSize + (2 * Long.BYTES) + SPLIT_THRESHOLD)) {
				unusedBlock = splitBlock(unusedBlock);
			}
		} else {
			unusedBlock = allocateMemoryAtEnd(requestedSize);
		}

		markBlockAsUsed(unusedBlock);
		return unusedBlock;
	}

	/**
	 * Allocates <b>always a new</b> block of memory which is equal or bigger than the <code>requestedSize</code>. This method will <b>never</b> return a
	 * {@link MemoryBlock} which is marked as <b>free</b>.
	 * <p>
	 * The returned block will be marked as <b>not free</b> and will never be returned by this method again, until marked as <b>free</b> again by calling
	 * {@link #freeMemory(MemoryBlock)}
	 * <p>
	 * A call to this method will result in {@link #getMemoryToUpdate() new pending updates}.
	 *
	 * @param requestedSize
	 * @return a new memory block
	 * @see MemoryBlock
	 * @see #allocateMemory(long)
	 * @see #freeMemory(MemoryBlock)
	 * @see #getMemoryToUpdate()
	 */
	public MemoryBlock allocateNewMemory(long requestedSize) {
		requestedSize = alignValue(requestedSize);
		final MemoryBlock block = createMemoryAtEnd(requestedSize);
		markBlockAsUsed(block);
		return block;
	}

	/**
	 * Will clear the memory model, this means the model no longer contains any {@link MemoryBlock MemoryBlocks} and has no waiting updates.
	 */
	public void clearMemoryModel() {
		unusedBlocks.clear();
		usedBlocks.clear();
		pendingUpdate.clear();
	}

	/**
	 * Returns the {@link MemoryBlock} at <code>position</code> or throws an exception if no {@link MemoryBlock} starts at <code>position</code>
	 *
	 * @param position
	 *            at which a {@link MemoryBlock} starts
	 * @return the {@link MemoryBlock} which starts at <code>position</code>
	 * @throws IllegalStateException
	 *             If not {@link MemoryBlock} starts at <code>position</code>
	 * @see #tryFindBlockAt(long)
	 */
	public MemoryBlock findBlockAt(long position) {
		final MemoryBlock block = tryFindBlockAt(position);
		if (block == null) {
			throw new IllegalStateException(String.format("Memory error. Unable to find memory block at position %d", position)); // TODO
		}
		return block;
	}

	/**
	 * Marks the given {@link MemoryBlock} as <b>free</b>
	 * <p>
	 * A call to this method may result in {@link #getMemoryToUpdate() new pending updates}.
	 *
	 * @param block
	 *            the block to be freed
	 * @see #allocateMemory(long)
	 * @see #getMemoryToUpdate()
	 */
	// TODO Merge adjacent unused blocks
	public void freeMemory(MemoryBlock block) {
		if (!block.free) {
			if (!usedBlocks.remove(block)) { // true, if collection doesn't contain block
				throw new IllegalStateException("Memory block is not referenced as used");
			}
			markBlockAsUnused(block);
		} else {
			if (!unusedBlocks.contains(block)) { // true, if collection doesn't contain block
				throw new IllegalStateException("Memory block is not referenced as unused");
			}
		}
	}

	/**
	 * Returns a set of {@link MemoryBlock MemoryBlocks} which <b>changed since the last call</b> to {@link #getMemoryToUpdate()}.<br>
	 * This set includes newly allocated, freed, merged and blocks which are split.
	 *
	 * @return a collection of blocks
	 */
	public Collection<MemoryBlock> getMemoryToUpdate() {
		final List<MemoryBlock> list = new ArrayList<>(pendingUpdate);
		pendingUpdate.clear();
		return list;
	}

	/**
	 * Add a {@link MemoryBlock} to the model. This method can be called multiple times to <b>initialize</b> the model. <br>
	 * This method must not be called after the model is already in use, the resulting behavior is undefined.
	 * <p>
	 * While new blocks can be added in any order, it is important to ensure that the whole set of blocks covers a sequentially part of <i>abstract</i> memory,
	 * without overlapping.
	 *
	 * @param blockPosition
	 * @param blockSize
	 * @param isFree
	 *
	 */
	public void setInitialBlock(long blockPosition, long blockSize, boolean isFree) {
		if (blockSize > Integer.MAX_VALUE) {
			throw new IntegerOverflowException("This implementation only supports a 'blockSize' of up to the maximum size of an integer");
		}

		if (blockPosition != alignValue(blockPosition)) {
			throw new IllegalArgumentException("'blockPosition' must be aligned to a 16-byte boundary"); // TODO
		}

		if (blockSize != alignValue(blockSize)) {
			throw new IllegalArgumentException(String.format("'blockSize' must be a multiple of 16. Was %d", blockSize)); // TODO
		}

		final MemoryBlock m = new MemoryBlock(blockPosition, (int) blockSize, isFree);
		if (m.free) {
			unusedBlocks.add(m);
		} else {
			usedBlocks.add(m);
		}
	}

	/**
	 * Tries to find a block that starts at <code>position</code>. <br>
	 * If there is no block, this method will return <code>null</code> to indicate this.
	 *
	 * @param position
	 * @return {@link MemoryBlock} at the given <code>position</code> or <code>null</code> if there is nothing.
	 * @see #findBlockAt(long)
	 */
	public MemoryBlock tryFindBlockAt(long position) {
		for (final MemoryBlock block : usedBlocks) {
			if (block.position == position) {
				return block;
			}
			if (block.position < block.position) {
				break;
			}
		}
		for (final MemoryBlock block : unusedBlocks) {
			if (block.position == position) {
				return block;
			}
			if (block.position < block.position) {
				break;
			}
		}
		return null;
	}

	public MemoryBlock tryFindBlockWithin(long position) {
		for (final MemoryBlock block : usedBlocks) {
			if ((block.position <= position) && (position <= (block.position + block.size))) {
				return block;
			}
			if (position < (block.position + block.size)) {
				break;
			}
		}
		for (final MemoryBlock block : unusedBlocks) {
			if ((block.position <= position) && (position <= (block.position + block.size))) {
				return block;
			}
			if (position < (block.position + block.size)) {
				break;
			}
		}
		return null;
	}

	private MemoryBlock allocateMemoryAtEnd(long requestedSize) {
		if (isEndOfMemoryUnused()) {
			final MemoryBlock lastBlock = unusedBlocks.last();
			if (lastBlock.size < requestedSize) {
				lastBlock.size = (int) requestedSize;
				pendingUpdate.add(lastBlock);
			}
			return lastBlock;
		} else {
			return createMemoryAtEnd(requestedSize);
		}
	}

	private MemoryBlock createMemoryAtEnd(long requestedSize) {
		final long endOfMemory = getEndOfMemory();
		final MemoryBlock unusedBlock = new MemoryBlock(alignValue(endOfMemory), (int) requestedSize, true);
		pendingUpdate.add(unusedBlock);
		unusedBlocks.add(unusedBlock);
		return unusedBlock;
	}

	private MemoryBlock findUnusedBlock(long requestedSize) {
		MemoryBlock bestFit = null;
		for (final MemoryBlock block : unusedBlocks) {
			if (block.size == requestedSize) {
				return block;
			} else if (block.size > requestedSize) {
				if (bestFit == null) {
					bestFit = block;
				} else if (block.size < bestFit.size) {
					bestFit = block;
				}
			}
		}
		return bestFit;
	}

	private long getEndOfMemory() {
		long endOfMemory = memOffset;
		if (!unusedBlocks.isEmpty()) {
			final MemoryBlock last = unusedBlocks.last();
			endOfMemory = Math.max(endOfMemory, last.position + last.size + Long.BYTES);
		}
		if (!usedBlocks.isEmpty()) {
			final MemoryBlock last = usedBlocks.last();
			endOfMemory = Math.max(endOfMemory, last.position + last.size + Long.BYTES);
		}
		return endOfMemory;
	}

	private boolean isEndOfMemoryUnused() {
		if (unusedBlocks.isEmpty()) {
			return false;
		}

		if (usedBlocks.isEmpty()) {
			return true;
		}

		return usedBlocks.last().position < unusedBlocks.last().position;
	}

	private void markBlockAsUnused(MemoryBlock block) {
		block.free = true;
		unusedBlocks.add(block);
		pendingUpdate.add(block);
		usedBlocks.remove(block);
	}

	private void markBlockAsUsed(MemoryBlock block) {
		block.free = false;
		usedBlocks.add(block);
		pendingUpdate.add(block);
		unusedBlocks.remove(block);
	}

	private MemoryBlock mergeBlocks(MemoryBlock first, MemoryBlock second) {
		// TODO
		return null;
	}

	private MemoryBlock splitBlock(MemoryBlock unusedBlock) {
		// TODO Auto-generated method stub
		return unusedBlock;
	}

}