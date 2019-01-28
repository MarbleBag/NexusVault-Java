package nexusvault.format.m3;

public interface ModelVertex {

	float getLocationX();

	float getLocationY();

	float getLocationZ();

	float[] getLocation(float[] dst, int dstOffset);

	int getBoneIndex1();

	int getBoneIndex2();

	int getBoneIndex3();

	int getBoneIndex4();

	// int getBoneIndex(int idx);
	int getBoneWeight1();

	int getBoneWeight2();

	int getBoneWeight3();

	int getBoneWeight4();

	// int getBoneWeight(int idx);
	float getTextureCoordU1();

	float getTextureCoordV1();

	float getTextureCoordU2();

	float getTextureCoordV2();

	float[] getTexCoords(float[] dst, int dstOffset);

}
