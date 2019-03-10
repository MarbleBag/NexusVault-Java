package nexusvault.archive.impl;

import java.io.IOException;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.io.util.Seek;
import nexusvault.archive.impl.ArchiveMemoryModel.MemoryBlock;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;
import nexusvault.shared.exception.IntegerOverflowException;

public class ArchivePackFile extends PackFile {

	private StructRootBlock rootElement;

	@Override
	protected void disposeResources() {
		super.disposeResources();
		rootElement = null;
	}

	@Override
	protected void readFileOnOpen(BinaryReader reader) {
		super.readFileOnOpen(reader);
		readRootElement(reader);
	}

	public StructRootBlock getRootElement() {
		checkFileState();
		return rootElement;
	}

	private void readRootElement(BinaryReader reader) {
		if (header.packRootIdx == -1) {
			return; // no root pack
		}

		if (header.packRootIdx > header.packCount) {
			throw new IllegalArgumentException(String.format("Archive File : Pack root idx %d exceeds pack count %d", header.packRootIdx, header.packCount));
		}
		if ((header.packRootIdx > Integer.MAX_VALUE) || (header.packRootIdx < 0)) {
			throw new IntegerOverflowException("Archive file: pack root");
		}

		final StructPackHeader rootPack = getPack(getPackRootIndex());
		reader.seek(Seek.BEGIN, rootPack.getOffset());

		final int signature = reader.readInt32();
		final int version = reader.readInt32();
		final int count = reader.readInt32();
		final int headerIdx = reader.readInt32();

		rootElement = new StructRootBlock(signature, version, count, headerIdx);
	}

	public StructPackHeader writeRootElement(StructRootBlock element) throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			return writeRootElement(writer, element);
		}
	}

	public StructPackHeader writeRootElement(BinaryWriter writer, StructRootBlock element) {
		StructPackHeader rootPack;
		if (header.packRootIdx != -1) { // element already set
			rootPack = getPack(getPackRootIndex());
			writer.seek(Seek.BEGIN, rootPack.getOffset());
		} else {
			final MemoryBlock memoryBlock = getMemoryModel().allocateMemory(StructRootBlock.SIZE_IN_BYTES);
			rootPack = new StructPackHeader(memoryBlock.position(), StructRootBlock.SIZE_IN_BYTES);
			writer.seek(Seek.BEGIN, memoryBlock.position());
		}

		writer.writeInt32(element.signature);
		writer.writeInt32(element.version);
		writer.writeInt32(element.entryCount);
		writer.writeInt32(element.headerIdx);
		rootElement = element;
		return rootPack;
	}

	public void updatePackRootIdx(long rootIdx) throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			updatePackRootIdx(writer, rootIdx);
		}
	}

	public long getPackRootIndex() {
		checkFileState();
		return header.packRootIdx;
	}

	public void updatePackRootIdx(BinaryWriter writer, long rootIdx) {
		if (header.packRootIdx != rootIdx) {
			header.packRootIdx = rootIdx;
			writer.seek(Seek.BEGIN, 0x228); // pack counter
			writer.writeInt64(header.packRootIdx);
		}
	}

}
