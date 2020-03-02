package nexusvault.format.m3.export.gltf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import nexusvault.format.m3.export.gltf.LookUp.LookUpEntry;

class MappedIndexLookUp<I, L, E extends LookUpEntry<I, L>> extends LookUp<I, L, E> {

	private final Map<I, E> map = new HashMap<>();

	@Override
	void store(E data) {
		map.put(data.getOriginalIndex(), data);
	}

	@Override
	public boolean hasEntry(I originalIndex) {
		return map.containsKey(originalIndex);
	}

	@Override
	public E getForOriginalIndex(I index) {
		return map.get(index);
	}

	@Override
	public E getForLookUpIndex(L index) {
		for (final Entry<I, E> entry : map.entrySet()) {
			if (index.equals(entry.getValue().getLookUpIndex())) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public Iterator<E> iterator() {
		return map.values().iterator();
	}

}