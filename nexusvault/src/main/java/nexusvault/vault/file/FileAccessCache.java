/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.vault.file;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public final class FileAccessCache {

	private final long cacheTime;
	private final Path filePath;

	private Thread thread;

	private volatile long lastUsed;
	private volatile boolean expiring;
	private volatile boolean dispose;

	private final Object lock = new Object();

	private SeekableByteChannel stream;
	private final EnumSet<StandardOpenOption> fileAccessOption;

	public FileAccessCache(long cacheTime, Path filePath, EnumSet<StandardOpenOption> fileAccessOption) {
		if (cacheTime <= 0) {
			throw new IllegalArgumentException();
		}
		if (filePath == null) {
			throw new IllegalArgumentException();
		}
		if (fileAccessOption == null) {
			throw new IllegalArgumentException();
		}

		this.cacheTime = cacheTime;
		this.filePath = filePath;
		this.fileAccessOption = fileAccessOption;
	}

	public void dispose() throws IOException {
		try {
			synchronized (this.lock) {
				closeChannel();
				this.dispose = true;
				if (this.thread != null) {
					this.thread.interrupt();
				}
			}
		} catch (final Throwable e) {
			throw new IOException(e);
		}
	}

	private void closeChannel() {
		try {
			this.stream.close();
		} catch (final Throwable e) {

		}
	}

	public boolean isDisposed() {
		return this.dispose;
	}

	public SeekableByteChannel getFileAccess() throws IOException {
		synchronized (this.lock) {
			if (this.dispose) {
				throw new IOException("disposed");
			}

			this.lastUsed = System.currentTimeMillis();
			this.expiring = false;

			if (this.stream == null || !this.stream.isOpen()) {
				if (this.fileAccessOption.contains(StandardOpenOption.CREATE) || this.fileAccessOption.contains(StandardOpenOption.CREATE_NEW)) {
					final var parent = this.filePath.getParent();
					if (parent != null && !Files.exists(parent)) {
						Files.createDirectories(parent);
					}
				}
				this.stream = Files.newByteChannel(this.filePath, this.fileAccessOption);
			}
		}
		return this.stream;
	}

	public void startExpiring() {
		if (this.expiring || this.dispose) {
			return;
		}

		synchronized (this.lock) {
			if (this.expiring || this.dispose) {
				return;
			}

			this.lastUsed = System.currentTimeMillis();
			this.expiring = true;

			if (this.thread != null && this.thread.isAlive()) {
				return;
			}

			this.thread = new Thread(() -> {
				boolean active = true;
				try {
					while (active && !this.dispose) {
						synchronized (this.lock) {
							if (this.expiring) {
								if (System.currentTimeMillis() > this.lastUsed + this.cacheTime) {
									active = false;
								}
							}
						}

						try {
							if (active) {
								Thread.sleep(1000);
							}
						} catch (final InterruptedException e1) {
							if (this.dispose) {
								break;
							}
						}
					}
				} finally {
					try {
						this.stream.close();
					} catch (final Throwable e) {

					}
				}
			}, "fileaccesscache-timer-" + this.filePath.getFileName().toString());
			this.thread.start();
		}
	}

	public Path getFile() {
		return this.filePath;
	}

}