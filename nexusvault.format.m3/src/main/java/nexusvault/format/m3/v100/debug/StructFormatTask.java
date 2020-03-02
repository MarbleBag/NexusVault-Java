package nexusvault.format.m3.v100.debug;

public final class StructFormatTask implements Task {

	private final long dataOffset;
	private final Class<?> structClass;
	private final int structCount;
	private final TaskOutput<? super Table> out;

	public StructFormatTask(long dataOffset, Class<?> structClass, int structCount, TaskOutput<? super Table> out) {
		this.dataOffset = dataOffset;
		this.structClass = structClass;
		this.structCount = structCount;
		this.out = out;
	}

	@Override
	public void runTask(DebugInfo debugger) {
		final StructFormater formater = debugger.getStructFormater(structClass);
		final Table table = formater.formatTable(debugger, dataOffset, structClass, structCount);
		out.setOutput(table);
	}

}