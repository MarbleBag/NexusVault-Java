package nexusvault.format.m3.v100;

import nexusvault.format.m3.Model;
import nexusvault.format.m3.v100.struct.StructM3Header;

public class ModelDebuger {

	public void debug(Model m3) {
		if (!(m3 instanceof InMemoryModel)) {
			throw new IllegalArgumentException("Unable to debug model. Type " + m3.getClass() + " not supported.");
		}

		final DataTracker modelData = ((InMemoryModel) m3).modelData;
		final StructM3Header modelHeader = ((InMemoryModel) m3).header;

		debug(modelHeader, modelData);
	}

	public void debug(StructM3Header modelHeader, DataTracker modelData) {

	}

}
