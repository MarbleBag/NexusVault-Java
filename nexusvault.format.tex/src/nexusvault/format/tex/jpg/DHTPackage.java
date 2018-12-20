package nexusvault.format.tex.jpg;

class DHTPackage {
	public final int destinationId;
	public final int tableClass;
	public final int[][] codes;

	public DHTPackage(int destinationId, int tableClass, int[][] codes) {
		super();
		this.destinationId = destinationId;
		this.tableClass = tableClass;
		this.codes = codes;
	}

	public DHTPackage(int destinationId, int tableClass, int[] nBits, int[] huffVals) {
		super();
		this.destinationId = destinationId;
		this.tableClass = tableClass;
		codes = new int[16][];

		int pos = 0;
		for (int i = 0; i < nBits.length; ++i) {
			codes[i] = new int[nBits[i]];
			for (int j = 0; j < nBits[i]; ++j) {
				codes[i][j] = huffVals[pos++];
			}
		}
	}

	// TODO
	public void toConsole() {
		System.out.println("Destination Id: " + destinationId);
		System.out.println("Table Class: " + tableClass);
		for (int nBits = 0; nBits < codes.length; nBits++) {
			final int[] words = codes[nBits];
			System.out.print(String.format("  Codes of length %02d bits (%03d total):", (nBits + 1), words.length));
			if (words.length == 0) {
				System.out.println();
			} else {
				for (final int code : words) {
					System.out.print(String.format("%02X ", code));
				}
				System.out.println();
			}
		}
	}
}