package nexusvault.format.tex.jpg.old;

interface ImageRegionWriter {
	void setImageSize(int imageWidth, int imageHeight);

	int getNumberOfSteps();

	void writeImageRegion(StackSet stack, byte[] output);
}
