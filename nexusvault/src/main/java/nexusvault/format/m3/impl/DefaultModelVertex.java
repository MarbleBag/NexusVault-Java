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

package nexusvault.format.m3.impl;

import java.util.Arrays;
import java.util.Objects;

import nexusvault.format.m3.Vertex;

/**
 * Internal implementation. May change without notice.
 */
public final class DefaultModelVertex implements Vertex {

	protected float[] xyz;
	protected int[] f3_unk1;
	protected int[] f3_unk2;
	protected int[] f3_unk3;
	protected int[] boneIndex;
	protected int[] boneWeight;
	protected int[] f4_unk3;
	protected int[] f4_unk4;
	protected float[] textureCoord;
	protected int f6_unk1;

	@Override
	public float getLocationX() {
		return this.xyz[0];
	}

	@Override
	public float getLocationY() {
		return this.xyz[1];
	}

	@Override
	public float getLocationZ() {
		return this.xyz[2];
	}

	@Override
	public int getUnknownData1_1() {
		return this.f3_unk1[0];
	}

	@Override
	public int getUnknownData1_2() {
		return this.f3_unk1[1];
	}

	@Override
	public int getUnknownData2_1() {
		return this.f3_unk2[0];
	}

	@Override
	public int getUnknownData2_2() {
		return this.f3_unk2[1];
	}

	@Override
	public int getUnknownData3_1() {
		return this.f3_unk3[0];
	}

	@Override
	public int getUnknownData3_2() {
		return this.f3_unk3[1];
	}

	@Override
	public int getBoneIndex1() {
		return this.boneIndex[0];
	}

	@Override
	public int getBoneIndex2() {
		return this.boneIndex[1];
	}

	@Override
	public int getBoneIndex3() {
		return this.boneIndex[2];
	}

	@Override
	public int getBoneIndex4() {
		return this.boneIndex[3];
	}

	@Override
	public int getBoneWeight1() {
		return this.boneWeight[0];
	}

	@Override
	public int getBoneWeight2() {
		return this.boneWeight[1];
	}

	@Override
	public int getBoneWeight3() {
		return this.boneWeight[2];
	}

	@Override
	public int getBoneWeight4() {
		return this.boneWeight[3];
	}

	@Override
	public int getUnknownData4_1() {
		return this.f4_unk3[0];
	}

	@Override
	public int getUnknownData4_2() {
		return this.f4_unk3[1];
	}

	@Override
	public int getUnknownData4_3() {
		return this.f4_unk3[2];
	}

	@Override
	public int getUnknownData4_4() {
		return this.f4_unk3[3];
	}

	@Override
	public int getUnknownData5_1() {
		return this.f4_unk4[0];
	}

	@Override
	public int getUnknownData5_2() {
		return this.f4_unk4[1];
	}

	@Override
	public int getUnknownData5_3() {
		return this.f4_unk4[2];
	}

	@Override
	public int getUnknownData5_4() {
		return this.f4_unk4[3];
	}

	@Override
	public float getTextureCoordU1() {
		return this.textureCoord[0];
	}

	@Override
	public float getTextureCoordV1() {
		return this.textureCoord[1];
	}

	@Override
	public float getTextureCoordU2() {
		return this.textureCoord[2];
	}

	@Override
	public float getTextureCoordV2() {
		return this.textureCoord[3];
	}

	@Override
	public int getUnknownData6_1() {
		return this.f6_unk1;
	}

	@Override
	public float[] getLocation(float[] dst, int dstOffset) {
		if (dst == null) {
			dst = new float[this.xyz.length];
			dstOffset = 0;
		}
		System.arraycopy(this.xyz, 0, dst, dstOffset, this.xyz.length);
		return dst;
	}

	@Override
	public float[] getTexCoords(float[] dst, int dstOffset) {
		if (dst == null) {
			dst = new float[this.textureCoord.length];
			dstOffset = 0;
		}
		System.arraycopy(this.textureCoord, 0, dst, dstOffset, this.textureCoord.length);
		return dst;
	}

	@Override
	public int[] getBoneIndex(int[] dst, int dstOffset) {
		if (dst == null) {
			dst = new int[this.boneIndex.length];
			dstOffset = 0;
		}
		System.arraycopy(this.boneIndex, 0, dst, dstOffset, this.boneIndex.length);
		return null;
	}

	@Override
	public int[] getBoneWeight(int[] dst, int dstOffset) {
		if (dst == null) {
			dst = new int[this.boneWeight.length];
			dstOffset = 0;
		}
		System.arraycopy(this.boneWeight, 0, dst, dstOffset, this.boneWeight.length);
		return dst;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.boneIndex);
		result = prime * result + Arrays.hashCode(this.boneWeight);
		result = prime * result + Arrays.hashCode(this.f3_unk1);
		result = prime * result + Arrays.hashCode(this.f3_unk2);
		result = prime * result + Arrays.hashCode(this.f3_unk3);
		result = prime * result + Arrays.hashCode(this.f4_unk3);
		result = prime * result + Arrays.hashCode(this.f4_unk4);
		result = prime * result + Arrays.hashCode(this.textureCoord);
		result = prime * result + Arrays.hashCode(this.xyz);
		result = prime * result + Objects.hash(this.f6_unk1);
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
		final DefaultModelVertex other = (DefaultModelVertex) obj;
		return Arrays.equals(this.boneIndex, other.boneIndex) && Arrays.equals(this.boneWeight, other.boneWeight) && Arrays.equals(this.f3_unk1, other.f3_unk1)
				&& Arrays.equals(this.f3_unk2, other.f3_unk2) && Arrays.equals(this.f3_unk3, other.f3_unk3) && Arrays.equals(this.f4_unk3, other.f4_unk3)
				&& Arrays.equals(this.f4_unk4, other.f4_unk4) && this.f6_unk1 == other.f6_unk1 && Arrays.equals(this.textureCoord, other.textureCoord)
				&& Arrays.equals(this.xyz, other.xyz);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Vertex [xyz=");
		builder.append(Arrays.toString(this.xyz));
		builder.append(", f3_unk1=");
		builder.append(Arrays.toString(this.f3_unk1));
		builder.append(", f3_unk2=");
		builder.append(Arrays.toString(this.f3_unk2));
		builder.append(", f3_unk3=");
		builder.append(Arrays.toString(this.f3_unk3));
		builder.append(", boneIndex=");
		builder.append(Arrays.toString(this.boneIndex));
		builder.append(", boneWeight=");
		builder.append(Arrays.toString(this.boneWeight));
		builder.append(", f4_unk3=");
		builder.append(Arrays.toString(this.f4_unk3));
		builder.append(", f4_unk4=");
		builder.append(Arrays.toString(this.f4_unk4));
		builder.append(", textureCoord=");
		builder.append(Arrays.toString(this.textureCoord));
		builder.append(", f6_unk1=");
		builder.append(this.f6_unk1);
		builder.append("]");
		return builder.toString();
	}

}