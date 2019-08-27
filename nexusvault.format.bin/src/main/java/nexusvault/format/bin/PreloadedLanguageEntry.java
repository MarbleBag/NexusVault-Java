package nexusvault.format.bin;

final class PreloadedLanguageEntry implements LanguageEntry {
	private final int id;
	private final String text;

	public PreloadedLanguageEntry(int id, String text) {
		super();
		this.id = id;
		this.text = text;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getText() {
		return text;
	}
}
