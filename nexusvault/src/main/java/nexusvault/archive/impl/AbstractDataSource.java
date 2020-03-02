package nexusvault.archive.impl;

import nexusvault.archive.NexusArchiveWriter.DataSource;
import nexusvault.archive.NexusArchiveWriter.DataSourceConfig;

public abstract class AbstractDataSource implements DataSource {
	private final DataSourceConfig config = new DataSourceConfig();

	@Override
	public DataSourceConfig getConfig() {
		return config;
	}
}