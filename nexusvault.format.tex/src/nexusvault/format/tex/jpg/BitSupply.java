package nexusvault.format.tex.jpg;

interface BitSupply {

	boolean canSupply(int nRequestedBits);

	int supply(int nRequestedBits);

}