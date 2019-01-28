package nexusvault.format.m3;

public interface ModelBone {

	int getBoneIndex();

	/**
	 * @return x - absolute location
	 */
	float getLocationX();

	/**
	 * @return y - absolute location
	 */
	float getLocationY();

	/**
	 * @return z - absolute location
	 */
	float getLocationZ();

	boolean hasParentBone();

	/**
	 * This method may return <tt>-1</tt> in case {@link #hasParentBone()} returned <tt>false</tt>.
	 *
	 * @return index to find its parent bone in {@link Model#getBones()}
	 */
	int getParentBoneReference();

	float[] getTransformationMatrix(int idx);

}
