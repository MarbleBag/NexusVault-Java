package nexusvault.format.tbl;

public final class TableRecord {

	protected final Object[] data;

	public TableRecord(int size) {
		this.data = new Object[size];
	}

	public Object get(int index) {
		return data[index];
	}

	public void set(int index, Object value) {
		data[index] = value;
	}

}
