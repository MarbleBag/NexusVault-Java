package nexusvault.format.m3;

public interface Vertex {

	float getLocationX();

	float getLocationY();

	float getLocationZ();

	float[] getLocation(float[] dst, int dstOffset);

	int getBoneIndex1();

	int getBoneIndex2();

	int getBoneIndex3();

	int getBoneIndex4();

	/**
	 * Stores all four bone indices in the given array
	 *
	 * @param dst
	 *            array to store data
	 * @param dstOffset
	 *            index at which the data should be added
	 * @return <code>dst</code> or a new array if <code>dst</code> was null
	 */
	int[] getBoneIndex(int[] dst, int dstOffset);

	int getBoneWeight1();

	int getBoneWeight2();

	int getBoneWeight3();

	int getBoneWeight4();

	int[] getBoneWeight(int[] dst, int dstOffset);

	float getTextureCoordU1();

	float getTextureCoordV1();

	float getTextureCoordU2();

	float getTextureCoordV2();

	float[] getTexCoords(float[] dst, int dstOffset);

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData1_1();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData1_2();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData2_1();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData2_2();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData3_1();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData3_2();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData4_1();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData4_2();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData4_3();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData4_4();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData5_1();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData5_2();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData5_3();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData5_4();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known. <br>
	 * The correct type of this data is unknown and is for simplicity returned as an (unsigned) integer.
	 * <p>
	 * The naming convention is <i>UnknownDataX_Y</i>, where X identifies the group and Y the index. Data with the same X belong together.
	 *
	 * @return 1 byte
	 */
	int getUnknownData6_1();
}
