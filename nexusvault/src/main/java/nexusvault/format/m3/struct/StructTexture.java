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

package nexusvault.format.m3.struct;

import java.util.Arrays;

import kreed.io.util.ByteBufferBinaryReader;
import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.format.m3.pointer.ATP_S2;
import nexusvault.shared.Text;
import nexusvault.shared.exception.StructException;

public final class StructTexture implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructTexture.class);

	static {
		if (SIZE_IN_BYTES != 0x20) {
			throw new StructException();
		}
	}

	// -1, -1 -> not set?
	// 1, 0 - > ?
	// 2, 0 -> ?
	// 5, 0 -> ?
	// 6, 0 -> ?
	@Order(1)
	@StructField(value = DataType.BIT_8, length = 2)
	public int[] unk_gap_000; // o: 0x000

	/**
	 * This may actually be a reference to assetTextureXY, found in model2display.tbl.
	 * <ul>
	 * <li>0 - diffuse
	 * <li>1 - normal
	 * <li>2 - FX(?)
	 * </ul>
	 */
	@Order(2)
	@StructField(DataType.BIT_8)
	public byte textureType; // o: 0x002

	@Order(3)
	@StructField(DataType.BIT_8)
	public byte unk_003; // o: 0x003

	@Order(4)
	@StructField(DataType.BIT_32)
	public int unk_value_004;

	@Order(5)
	@StructField(value = DataType.BIT_8, length = 8)
	public int[] unk_gap_008;

	/**
	 * UTF16 encoded. 2 bytes per character. Pointer contains length, but string is also 0-terminated. In some cases the given length is too long for the
	 * string, resulting in null-characters at the end of the string
	 */
	@Order(6)
	@StructField(DataType.STRUCT)
	public ATP_S2 textureName;

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, this.textureName);
	}

	public String getName(BytePositionTracker data) {
		data.setPosition(this.textureName.getOffset());
		return Text.readNullTerminatedUTF16(new ByteBufferBinaryReader(data.getData()));
		// data.setPosition(textureName.getOffset());
		// final Charset charset = ByteBufferUtil.getUTF16CharSet(data.getData());
		// final int numberOfBytes = (textureName.getArraySize() - 1) * 2; // 0 terminated
		// this.name = ByteBufferUtil.getString(data.getData(), numberOfBytes, charset);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructTexture [unk_gap_000=");
		builder.append(Arrays.toString(this.unk_gap_000));
		builder.append(", textureType=");
		builder.append(this.textureType);
		builder.append(", unk_003=");
		builder.append(this.unk_003);
		builder.append(", unk_value_004=");
		builder.append(this.unk_value_004);
		builder.append(", unk_gap_008=");
		builder.append(Arrays.toString(this.unk_gap_008));
		builder.append(", textureName=");
		builder.append(this.textureName);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.textureName == null ? 0 : this.textureName.hashCode());
		result = prime * result + this.textureType;
		result = prime * result + this.unk_003;
		result = prime * result + Arrays.hashCode(this.unk_gap_000);
		result = prime * result + Arrays.hashCode(this.unk_gap_008);
		result = prime * result + this.unk_value_004;
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
		final StructTexture other = (StructTexture) obj;
		if (this.textureName == null) {
			if (other.textureName != null) {
				return false;
			}
		} else if (!this.textureName.equals(other.textureName)) {
			return false;
		}
		if (this.textureType != other.textureType) {
			return false;
		}
		if (this.unk_003 != other.unk_003) {
			return false;
		}
		if (!Arrays.equals(this.unk_gap_000, other.unk_gap_000)) {
			return false;
		}
		if (!Arrays.equals(this.unk_gap_008, other.unk_gap_008)) {
			return false;
		}
		if (this.unk_value_004 != other.unk_value_004) {
			return false;
		}
		return true;
	}

}
