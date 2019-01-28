package nexusvault.format.m3;

import java.util.Iterator;

@Deprecated
public interface ModelVertexIterator extends Iterator<ModelVertex> {

	@Override
	boolean hasNext();

	@Override
	ModelVertex next();

	boolean hasPrevious();

	ModelVertex previous();

	// TODO

}
