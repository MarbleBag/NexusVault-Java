package nexusvault.format.m3.v100.debug;

public final class DoubleStructFormatTask implements Task {
	private final long offsetA;
	private final Class<?> structClassA;
	private final long offsetB;
	private final Class<?> structClassB;
	private final int structCount;
	private final TaskOutput<? super Table> out;

	public DoubleStructFormatTask(long offsetA, Class<?> structClassA, long offsetB, Class<?> structClassB, int structCount, TaskOutput<? super Table> out) {
		this.offsetA = offsetA;
		this.structClassA = structClassA;
		this.offsetB = offsetB;
		this.structClassB = structClassB;
		this.structCount = structCount;
		this.out = out;
	}

	@Override
	public void runTask(DebugInfo debugger) {
		final StructFormater formaterA = debugger.getStructFormater(structClassA);
		final Table tableA = formaterA.formatTable(debugger, offsetA, structClassA, structCount);

		final StructFormater formaterB = debugger.getStructFormater(structClassB);
		final Table tableB = formaterB.formatTable(debugger, offsetB, structClassB, structCount);

		out.setOutput(Table.mergeColumns(tableA, tableB));
	}
}