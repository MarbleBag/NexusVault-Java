package nexusvault.format.tbl.better;

import nexusvault.format.tbl.struct.DataType;

public class Column {

	public String name;
	public DataType dataType;
	public long unk1;
	public long unk2;

	public Column(String name, DataType dataType, long unk1, long unk2) {
		this.name = name;
		this.dataType = dataType;
		this.unk1 = unk1;
		this.unk2 = unk2;
	}
}
