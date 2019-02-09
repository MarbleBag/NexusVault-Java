package nexusvault.format.m3.v100;

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

	@Deprecated
	public int getF3_Unk1_1() {
		return f3_unk1[0];
	}

	@Deprecated
	public int getF3_Unk1_2() {
		return f3_unk1[1];
	}

	@Deprecated
	public int getF3_Unk1_3() {
		return f3_unk1[2];
	}

	@Deprecated
	public int getF3_Unk2_1() {
		return f3_unk2[0];
	}

	@Deprecated
	public int getF3_Unk2_2() {
		return f3_unk2[1];
	}

	@Deprecated
	public int getF3_Unk2_3() {
		return f3_unk2[2];
	}

	@Deprecated
	public int getF3_Unk3_1() {
		return f3_unk3[0];
	}

	@Deprecated
	public int getF3_Unk3_2() {
		return f3_unk3[1];
	}

	@Deprecated
	public int getF3_Unk3_3() {
		return f3_unk3[2];
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

	@Deprecated
	public int getF4_Unk3_1() {
		return f4_unk3[0];
	}

	@Deprecated
	public int getF4_Unk3_2() {
		return f4_unk3[1];
	}

	@Deprecated
	public int getF4_Unk3_3() {
		return f4_unk3[2];
	}

	@Deprecated
	public int getF4_Unk3_4() {
		return f4_unk3[3];
	}

	@Deprecated
	public int getF4_Unk4_1() {
		return f4_unk4[0];
	}

	@Deprecated
	public int getF4_Unk4_2() {
		return f4_unk4[1];
	}

	@Deprecated
	public int getF4_Unk4_3() {
		return f4_unk4[2];
	}

	@Deprecated
	public int getF4_Unk4_4() {
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

	@Deprecated
	public int getF6_Unk1() {
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

}