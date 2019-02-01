package nexusvault.format.m3.v100.debug;

import java.util.List;

import nexusvault.format.m3.v100.DataTracker;

public interface Util {
	List<Object> loadStructs(long dataOffset, Class<?> structClass, int structCount);

	DataTracker getDataModel();

	StructFormater getStructFormater(Class<?> structClass);

	void queueTask(Task task);
}