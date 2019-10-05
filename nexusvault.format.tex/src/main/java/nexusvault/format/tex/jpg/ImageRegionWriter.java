package nexusvault.format.tex.jpg;

import nexusvault.format.tex.jpg.tool.StackSet;

interface ImageRegionWriter {
	void setImageSize(int imageWidth, int imageHeight);

	int getNumberOfSteps();

	void writeImageRegion(StackSet stack, byte[] output);
}
