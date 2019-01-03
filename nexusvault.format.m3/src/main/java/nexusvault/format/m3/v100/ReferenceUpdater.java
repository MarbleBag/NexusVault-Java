package nexusvault.format.m3.v100;

import java.nio.ByteBuffer;

import kreed.reflection.struct.DataReadDelegator;
import kreed.reflection.struct.DataWriteDelegator;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructReader;
import kreed.reflection.struct.StructUtil;
import kreed.reflection.struct.StructWriter;
import kreed.reflection.struct.reader.ByteBufferReader;
import kreed.reflection.struct.writer.ByteBufferWriter;
import nexusvault.format.m3.v100.pointer.ArrayTypePointer;
import nexusvault.format.m3.v100.pointer.DoubleArrayTypePointer;
import nexusvault.shared.exception.IntegerOverflowException;

class ReferenceUpdater implements StructVisitor {

	private final StructReader<ByteBuffer> structBuilder;
	private final StructWriter<ByteBuffer> structSaver;

	public ReferenceUpdater() {
		structBuilder = StructReader.build(StructFactory.build(), DataReadDelegator.build(new ByteBufferReader()), true);
		structSaver = StructWriter.build(StructFactory.build(), DataWriteDelegator.build(new ByteBufferWriter()), true);
	}

	public <T extends VisitableStruct> T start(DataTracker fileReader, Class<? extends T> initialStruct) {
		final int nextDataPosition = padPosition(fileReader.getDataStart(), 1, StructUtil.sizeOf(initialStruct));

		fileReader.resetPosition();
		final T struct = structBuilder.read(initialStruct, fileReader.getData());
		struct.visit(this, fileReader, nextDataPosition);

		fileReader.resetPosition();
		structSaver.write(struct, fileReader.getData());

		return struct;
	}

	@Override
	public void process(DataTracker fileReader, int dataPosition, ArrayTypePointer pointer) {
		final int totalSize = pointer.getArraySize() * pointer.getElementSize();
		final int blockStart = (int) (pointer.getOffset() + dataPosition);
		final int blockEnd = blockStart + totalSize;
		if (blockStart < fileReader.getDataStart()) {
			throw new IndexOutOfBoundsException(
					"Pointer is below file start: Start at: " + fileReader.getDataStart() + " Pointer: " + pointer + " -> " + blockStart);
		}
		if (blockEnd > fileReader.getDataEnd()) {
			throw new IndexOutOfBoundsException("Pointer is above file end: End at: " + fileReader.getDataEnd() + " Pointer: " + pointer + " -> " + blockEnd);
		}
		pointer.setOffset(pointer.hasElements() ? blockStart : 0);

		if (pointer.hasType()) {
			final int nextDataPosition = padPosition(pointer);
			visitStruct(blockStart, pointer.getElementSize(), pointer.getArraySize(), pointer.getTypeOfElement(), fileReader, nextDataPosition);
		}
	}

	@Override
	public void process(DataTracker fileReader, int dataPosition, DoubleArrayTypePointer pointer) {
		{
			final int totalSizeA = pointer.getArraySize() * pointer.getElementSizeA();
			final int blockStartA = (int) (pointer.getOffsetA() + dataPosition);
			final int blockEndA = blockStartA + totalSizeA;
			if (blockEndA < fileReader.getDataStart()) {
				throw new IndexOutOfBoundsException(
						"Pointer A is below file start: Start at: " + fileReader.getDataStart() + " Pointer: " + pointer + " -> " + blockStartA);
			}
			if (blockEndA > fileReader.getDataEnd()) {
				throw new IndexOutOfBoundsException(
						"Pointer A is below file start: Start at: " + fileReader.getDataStart() + " Pointer: " + pointer + " -> " + blockEndA);
			}
			pointer.setOffsetA(pointer.hasElements() ? blockStartA : 0);

			if (pointer.hasTypeA()) {
				final int nextDataPosition = padPositionA(pointer);
				visitStruct(blockStartA, pointer.getElementSizeA(), pointer.getArraySize(), pointer.getTypeOfElementA(), fileReader, nextDataPosition);
			}
		}

		{
			final int totalSizeB = pointer.getArraySize() * pointer.getElementSizeB();
			final int blockStartB = (int) (pointer.getOffsetB() + dataPosition);
			final int blockEndB = blockStartB + totalSizeB;
			if (blockEndB < fileReader.getDataStart()) {
				throw new IndexOutOfBoundsException(
						"Pointer B is below file start: Start at: " + fileReader.getDataStart() + " Pointer: " + pointer + " -> " + blockStartB);
			}
			if (blockEndB > fileReader.getDataEnd()) {
				throw new IndexOutOfBoundsException(
						"Pointer B is below file start: Start at: " + fileReader.getDataStart() + " Pointer: " + pointer + " -> " + blockEndB);
			}
			pointer.setOffsetB(pointer.hasElements() ? blockStartB : 0);

			if (pointer.hasTypeB()) {
				final int nextDataPosition = padPositionB(pointer);
				visitStruct(blockStartB, pointer.getElementSizeB(), pointer.getArraySize(), pointer.getTypeOfElementB(), fileReader, nextDataPosition);
			}
		}
	}

	private void visitStruct(int structStart, int structSize, int structCount, Class<? extends VisitableStruct> structType, DataTracker data, int dataOffset) {
		for (int idx = 0; idx < structCount; ++idx) {
			final int structPosition = structStart + (idx * structSize);
			data.setPosition(structPosition);
			final VisitableStruct struct = structBuilder.read(structType, data.getData());
			struct.visit(this, data, dataOffset);
			data.setPosition(structPosition);
			structSaver.write(struct, data.getData());
		}
	}

	private int padPosition(ArrayTypePointer pointer) {
		return padPosition(pointer.getOffset(), pointer.getArraySize(), pointer.getElementSize());
	}

	private int padPositionA(DoubleArrayTypePointer pointer) {
		return padPosition(pointer.getOffsetA(), pointer.getArraySize(), pointer.getElementSizeA());
	}

	private int padPositionB(DoubleArrayTypePointer pointer) {
		return padPosition(pointer.getOffsetB(), pointer.getArraySize(), pointer.getElementSizeB());
	}

	private int padPosition(long offset, int units, int sizeInBytes) {
		final long paddedPosition = (offset + (((units * sizeInBytes) + 15) & 0xFFFFFFFFFFFFF0L));
		if ((paddedPosition > Integer.MAX_VALUE) || (paddedPosition < 0)) {
			throw new IntegerOverflowException();
		}
		return (int) paddedPosition;
	}

}
