package nexusvault.format.m3.v100.struct;

import kreed.reflection.struct.StructUtil;

abstract class SizeTest {
	private SizeTest() {

	}

	public static void ensureSizeAndOrder(Class<?> structClass, int expectedSize) {
		System.out.println(StructUtil.analyzeStruct(structClass, true));
		final int size = StructUtil.sizeOf(structClass);
		if (size != expectedSize) {
			throw new IllegalStateException(String.format("Struct %s: Expected size %d, was %d", structClass, expectedSize, size));
		}
	}

}
