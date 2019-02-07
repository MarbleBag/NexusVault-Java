package nexusvault.format.m3.v100.pointer;

import nexusvault.format.m3.v100.struct.StructUInt32;
import nexusvault.format.m3.v100.struct.StructUInt8;

public final class DATP_UInt32_UInt8 extends DoubleArrayTypePointer<StructUInt32, StructUInt8> {
	public DATP_UInt32_UInt8() {
		super(StructUInt32.class, StructUInt8.class);
	}
}