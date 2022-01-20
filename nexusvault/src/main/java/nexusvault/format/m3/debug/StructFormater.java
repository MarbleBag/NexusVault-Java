package nexusvault.format.m3.debug;

public interface StructFormater {
	Table formatTable(DebugInfo debugger, long dataOffset, Class<?> structClass, int structCount);
}