package nexusvault.format.tbl;

public final class TableRecord {

	public final Object[] data;

	public TableRecord(int size) {
		this.data = new Object[size];
	}

	public Object get(int index) {
		return this.data[index];
	}

	public void set(int index, Object value) {
		this.data[index] = value;
	}

}
