package nexusvault.archive.impl;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxPath;

interface BaseRootIdxDirectory extends IdxDirectory {

	void setArchive(BaseNexusArchive archive);

}

final class BaseLazyRootIdxDirectory extends BaseLazyIdxDirectory implements BaseRootIdxDirectory {

	private BaseNexusArchive archive;

	public BaseLazyRootIdxDirectory(int headerIndex) {
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

	@Override
	public void setArchive(BaseNexusArchive archive) {
		this.archive = archive;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("IdxRootDirectory [");
		builder.append("headerIdx=").append(getDirectoryPackIndex());
		builder.append(", #childs=").append(getChilds().size());
		builder.append("]");
		return builder.toString();
	}

}
