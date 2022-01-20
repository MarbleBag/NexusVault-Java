package nexusvault.format.m3.debug;

import java.util.List;

import nexusvault.format.m3.impl.BytePositionTracker;

public interface DebugInfo {
	List<Object> loadStructs(long dataOffset, Class<?> structClass, int structCount);

	BytePositionTracker getDataModel();

	StructFormater getStructFormater(Class<?> structClass);

	void queueTask(Task task);
}