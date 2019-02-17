package nexusvault.archive.impl;

import nexusvault.archive.IdxPath;

final class BaseRootIdxDirectory extends BaseIdxDirectory {

	private BaseNexusArchive archive;

	public BaseRootIdxDirectory(int headerIndex) {
		super(null, "", headerIndex);
	}

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public IdxPath getPath() {
		return new BaseIdxPath();
	}

	@Override
	public BaseNexusArchive getArchive() {
		return archive;
	}

}
