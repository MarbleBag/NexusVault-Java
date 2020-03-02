package nexusvault.format.m3.export.gltf;

import nexusvault.format.m3.export.gltf.LookUp.LookUpEntry;

abstract class LookUp<I, L, E extends LookUpEntry<I, L>> implements Iterable<E> {

	static class LookUpEntry<I, L> {
		private final I originalIndex;
		private L lookUpIndex;

		public LookUpEntry(I originalIndex, L lookUpIndex) {
			this.originalIndex = originalIndex;
			this.lookUpIndex = lookUpIndex;
		}

		public void setLookUpIndex(L lookUpIndex) {
			this.lookUpIndex = lookUpIndex;
		}

		public I getOriginalIndex() {
			return originalIndex;
		}

		public L getLookUpIndex() {
			return lookUpIndex;
		}
	}

	public void add(E data) {
		if (hasEntry(data.getOriginalIndex())) {
			throw new IllegalArgumentException("Index already defined");
		}
		store(data);
	}

	public void set(E data) {
		store(data);
	}

	abstract void store(E data);

	abstract public boolean hasEntry(I originalIndex);

	abstract public E getForOriginalIndex(I index);

	abstract public E getForLookUpIndex(L index);
}