package nexusvault.format.m3.v100;

import java.util.Arrays;
import java.util.Objects;

import nexusvault.format.m3.ModelVertex;

/**
 * Internal implementation. May change without notice.
 */
final class DefaultModelVertex implements ModelVertex {

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
		return xyz[0];
	}

	@Override
	public float getLocationY() {
		return xyz[1];
	}

	@Override
	public float getLocationZ() {
		return xyz[2];
	}

	@Override
	public int getUnknownData1_1() {
		return f3_unk1[0];
	}

	@Override
	public int getUnknownData1_2() {
		return f3_unk1[1];
	}

	@Override
	public int getUnknownData2_1() {
		return f3_unk2[0];
	}

	@Override
	public int getUnknownData2_2() {
		return f3_unk2[1];
	}

	@Override
	public int getUnknownData3_1() {
		return f3_unk3[0];
	}

	@Override
	public int getUnknownData3_2() {
		return f3_unk3[1];
	}

	@Override
	public int getBoneIndex1() {
		return boneIndex[0];
	}

	@Override
	public int getBoneIndex2() {
		return boneIndex[1];
	}

	@Override
	public int getBoneIndex3() {
		return boneIndex[2];
	}

	@Override
	public int getBoneIndex4() {
		return boneIndex[3];
	}

	@Override
	public int getBoneWeight1() {
		return boneWeight[0];
	}

	@Override
	public int getBoneWeight2() {
		return boneWeight[1];
	}

	@Override
	public int getBoneWeight3() {
		return boneWeight[2];
	}

	@Override
	public int getBoneWeight4() {
		return boneWeight[3];
	}

	@Override
	public int getUnknownData4_1() {
		return f4_unk3[0];
	}

	@Override
	public int getUnknownData4_2() {
		return f4_unk3[1];
	}

	@Override
	public int getUnknownData4_3() {
		return f4_unk3[2];
	}

	@Override
	public int getUnknownData4_4() {
		return f4_unk3[3];
	}

	@Override
	public int getUnknownData5_1() {
		return f4_unk4[0];
	}

	@Override
	public int getUnknownData5_2() {
		return f4_unk4[1];
	}

	@Override
	public int getUnknownData5_3() {
		return f4_unk4[2];
	}

	@Override
	public int getUnknownData5_4() {
		return f4_unk4[3];
	}

	@Override
	public float getTextureCoordU1() {
		return textureCoord[0];
	}

	@Override
	public float getTextureCoordV1() {
		return textureCoord[1];
	}

	@Override
	public float getTextureCoordU2() {
		return textureCoord[2];
	}

	@Override
	public float getTextureCoordV2() {
		return textureCoord[3];
	}

	@Override
	public int getUnknownData6_1() {
		return f6_unk1;
	}

	@Override
	public float[] getLocation(float[] dst, int dstOffset) {
		if (dst == null) {
			dst = new float[xyz.length];
			dstOffset = 0;
		}
		System.arraycopy(xyz, 0, dst, dstOffset, xyz.length);
		return dst;
	}

	@Override
	public float[] getTexCoords(float[] dst, int dstOffset) {
		if (dst == null) {
			dst = new float[textureCoord.length];
			dstOffset = 0;
		}
		System.arraycopy(textureCoord, 0, dst, dstOffset, textureCoord.length);
		return dst;
	}

	@Override
	public int[] getBoneIndex(int[] dst, int dstOffset) {
		if (dst == null) {
			dst = new int[boneIndex.length];
			dstOffset = 0;
		}
		System.arraycopy(boneIndex, 0, dst, dstOffset, boneIndex.length);
		return null;
	}

	@Override
	public int[] getBoneWeight(int[] dst, int dstOffset) {
		if (dst == null) {
			dst = new int[boneWeight.length];
			dstOffset = 0;
		}
		System.arraycopy(boneWeight, 0, dst, dstOffset, boneWeight.length);
		return dst;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(boneIndex);
		result = (prime * result) + Arrays.hashCode(boneWeight);
		result = (prime * result) + Arrays.hashCode(f3_unk1);
		result = (prime * result) + Arrays.hashCode(f3_unk2);
		result = (prime * result) + Arrays.hashCode(f3_unk3);
		result = (prime * result) + Arrays.hashCode(f4_unk3);
		result = (prime * result) + Arrays.hashCode(f4_unk4);
		result = (prime * result) + Arrays.hashCode(textureCoord);
		result = (prime * result) + Arrays.hashCode(xyz);
		result = (prime * result) + Objects.hash(f6_unk1);
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
		return Arrays.equals(boneIndex, other.boneIndex) && Arrays.equals(boneWeight, other.boneWeight) && Arrays.equals(f3_unk1, other.f3_unk1)
				&& Arrays.equals(f3_unk2, other.f3_unk2) && Arrays.equals(f3_unk3, other.f3_unk3) && Arrays.equals(f4_unk3, other.f4_unk3)
				&& Arrays.equals(f4_unk4, other.f4_unk4) && (f6_unk1 == other.f6_unk1) && Arrays.equals(textureCoord, other.textureCoord)
				&& Arrays.equals(xyz, other.xyz);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Vertex [xyz=");
		builder.append(Arrays.toString(xyz));
		builder.append(", f3_unk1=");
		builder.append(Arrays.toString(f3_unk1));
		builder.append(", f3_unk2=");
		builder.append(Arrays.toString(f3_unk2));
		builder.append(", f3_unk3=");
		builder.append(Arrays.toString(f3_unk3));
		builder.append(", boneIndex=");
		builder.append(Arrays.toString(boneIndex));
		builder.append(", boneWeight=");
		builder.append(Arrays.toString(boneWeight));
		builder.append(", f4_unk3=");
		builder.append(Arrays.toString(f4_unk3));
		builder.append(", f4_unk4=");
		builder.append(Arrays.toString(f4_unk4));
		builder.append(", textureCoord=");
		builder.append(Arrays.toString(textureCoord));
		builder.append(", f6_unk1=");
		builder.append(f6_unk1);
		builder.append("]");
		return builder.toString();
	}

}