package nexusvault.format.m3;

import java.nio.ByteOrder;
import java.util.List;

// TODO In work
public interface ModelVertexReader {

	public static interface ModelVertexField {
		VertexField type();

		int position();

		int length();
	}

	List<ModelVertexField> getFields();

	int getVertexSizeInBytes();

	int getVertexCount();

	// boolean nextVertex();

	// boolean previousVertex();

	void moveToVertex(int index);

	int[] readFieldInt(ModelVertexField field, int[] store, int offset);

	float[] readFieldFloat(ModelVertexField field, float[] store, int offset);

	// byte[] readFieldByte(ModelVertexField field, byte[] store, int offset);

	byte[] readVertex(byte[] store, int offset);

	byte[] readVertex(byte[] store, int offset, List<ModelVertexField> fields);

	ByteOrder getByteOrder();

	ModelVertex readVertex();
}
