package nexusvault.format.m3.v100.debug;

import java.io.Writer;

public class ModelDataDebuger2 {

	private static interface PrintFormatter {
		void printArrayIndex(Writer writer, int size);

		void printArrayElement(Writer writer, Object value);
	}

}
