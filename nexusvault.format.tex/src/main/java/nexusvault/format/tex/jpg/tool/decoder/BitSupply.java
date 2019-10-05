package nexusvault.format.tex.jpg.tool.decoder;

public interface BitSupply {

	boolean canSupply(int nRequestedBits);

	int supply(int nRequestedBits);

}