package nexusvault.archive.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import nexusvault.shared.exception.IntegerOverflowException;

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

		public int size() {
			return size;
		}

		public long position() {
			return position;
		}

		public boolean isFree() {
			return free;
		}

		@Override
		public int compareTo(MemoryBlock o) {
			final long diff = position - o.position;
			return diff < 0 ? -1 : diff > 0 ? 1 : 0;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + (int) (position ^ (position >>> 32));
			return result;
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

		public void clearUpdateFlag() {
			ArchiveMemoryModel.this.pendingUpdate.remove(this);
		}
	}

	private static long alignValue(long pos) {
		return (pos + 0xF) & 0xFFFFFFFFFFFFFFF0l;
	}

	private static final long SPLIT_THRESHOLD = alignValue(6 * Long.BYTES);

	private final SortedSet<MemoryBlock> unusedBlocks = new TreeSet<>();
	private final SortedSet<MemoryBlock> usedBlocks = new TreeSet<>();
	private final SortedSet<MemoryBlock> pendingUpdate = new TreeSet<>();
	private final long memOffset;

	public ArchiveMemoryModel(long memOffset) {
		this.memOffset = alignValue(memOffset);
	}

	public void setInitialBlock(long blockPosition, long blockSize, boolean isFree) {
		if (blockSize > Integer.MAX_VALUE) {
			throw new IntegerOverflowException(); // TODO
		}

		if (blockPosition != alignValue(blockPosition)) {
			throw new IllegalArgumentException(); // TODO
		}

		if (blockSize != alignValue(blockSize)) {
			throw new IllegalArgumentException(); // TODO
		}

		final MemoryBlock m = new MemoryBlock(blockPosition, (int) blockSize, isFree);
		if (m.free) {
			unusedBlocks.add(m);
		} else {
			usedBlocks.add(m);
		}
	}

	public void clear() {
		unusedBlocks.clear();
		usedBlocks.clear();
		pendingUpdate.clear();
	}

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

	public MemoryBlock findBlockAt(long position) {
		final MemoryBlock block = tryFindBlockAt(position);
		if (block == null) {
			throw new IllegalStateException(String.format("Memory error. Unable to find memory block for offset %d", position)); // TODO
		}
		return block;
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

	private MemoryBlock splitBlock(MemoryBlock unusedBlock) {
		// TODO Auto-generated method stub
		return unusedBlock;
	}

	private MemoryBlock mergeBlocks(MemoryBlock first, MemoryBlock second) {
		// TODO
		return null;
	}

	private void markBlockAsUsed(MemoryBlock block) {
		block.free = false;
		usedBlocks.add(block);
		pendingUpdate.add(block);
		unusedBlocks.remove(block);
	}

	private void markBlockAsUnused(MemoryBlock block) {
		block.free = true;
		unusedBlocks.add(block);
		pendingUpdate.add(block);
		usedBlocks.remove(block);
	}

	public MemoryBlock allocateNewMemory(long requestedSize) {
		requestedSize = alignValue(requestedSize);
		final MemoryBlock block = createMemoryAtEnd(requestedSize);
		markBlockAsUsed(block);
		return block;
	}

	public void freeMemory(MemoryBlock block) {
		if (!block.free) {
			if (!usedBlocks.remove(block)) {
				throw new IllegalStateException(); // TODO
			}
			markBlockAsUnused(block);
		} else {
			if (!unusedBlocks.contains(block)) {
				throw new IllegalStateException(); // TODO
			}
		}
	}

	public Collection<MemoryBlock> getMemoryToUpdate() {
		final List<MemoryBlock> list = new ArrayList<>(pendingUpdate);
		pendingUpdate.clear();
		return list;
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

	private MemoryBlock allocateMemoryAtEnd(long requestedSize) {
		if (isEndOfMemoryUnused()) {
			final MemoryBlock lastBlock = this.unusedBlocks.last();
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

	private boolean isEndOfMemoryUnused() {
		if (unusedBlocks.isEmpty()) {
			return false;
		}

		if (usedBlocks.isEmpty()) {
			return true;
		}

		return usedBlocks.last().position < unusedBlocks.last().position;
	}

}