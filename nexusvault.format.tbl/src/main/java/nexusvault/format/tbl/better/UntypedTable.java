package nexusvault.format.tbl.better;

import java.util.List;

public class UntypedTable extends XTable<Object[]> {
	public UntypedTable(String name, Column[] columns, List<Object[]> entries, int[] lookup) {
		super(name, columns, entries, lookup);
	}
}
