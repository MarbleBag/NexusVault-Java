package nexusvault.format.bin;

public class DictionaryConfig {

	private boolean lazyLoading = false;
	private boolean caching;

	/**
	 * If set to <code>true</code>, the reader creates a dictionary which loads its string lazily from the given source. This will speed up the creation of
	 * large dictionaries. The read values will by default not be cached by the dictionary.
	 * <p>
	 * To be able to load its entries lazily, the dictionary will keep a reference to its source around, this may increase memory footprint in combination with
	 * String caching.
	 *
	 * @param value
	 *            true will switch to lazy loading
	 * @see #setCaching(boolean)
	 */
	public void setLazyLoading(boolean value) {
		lazyLoading = value;
	}

	public boolean getLazyLoading() {
		return lazyLoading;
	}

	/**
	 * If set to <code>true</code>, the reader creates a dictionary which will cache its entries on request. Is the created dictionary not lazy loading, this
	 * setting will be ignored.
	 *
	 * @param value
	 *            true will activate caching
	 * @see #setLazyLoading(boolean)
	 */
	public void setCaching(boolean value) {
		caching = value;
	}

	public boolean getCaching() {
		return caching;
	}

}
