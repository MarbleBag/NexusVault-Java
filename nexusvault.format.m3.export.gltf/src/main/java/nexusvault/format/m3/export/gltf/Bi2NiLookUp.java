package nexusvault.format.m3.export.gltf;

import nexusvault.format.m3.export.gltf.Bi2NiLookUp.Bi2NiEntry;

final class Bi2NiLookUp extends MappedIndexLookUp<Integer, Integer, Bi2NiEntry> {
	static final class Bi2NiEntry extends nexusvault.format.m3.export.gltf.LookUp.LookUpEntry<Integer, Integer> {

		private Integer parentOriginalIndex;

		public Bi2NiEntry(Integer originalIndex, Integer lookUpIndex) {
			super(originalIndex, lookUpIndex);
		}

		public boolean hasParent() {
			return parentOriginalIndex != null;
		}

		public Integer getParentOriginalIndex() {
			return parentOriginalIndex;
		}

		public void setParentOriginalIndex(Integer index) {
			parentOriginalIndex = index;
		}

	}
}
