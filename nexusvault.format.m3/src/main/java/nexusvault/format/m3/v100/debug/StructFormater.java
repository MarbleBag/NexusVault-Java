package nexusvault.format.m3.v100.debug;

public interface StructFormater {
	Table formatTable(Util util, long dataOffset, Class<?> structClass, int structCount);
}