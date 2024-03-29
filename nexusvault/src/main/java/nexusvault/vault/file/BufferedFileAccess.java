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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryReaderDelegate;
import kreed.io.util.BinaryWriter;
import kreed.io.util.BinaryWriterDelegate;
import kreed.io.util.SeekableByteChannelBinaryReader;
import kreed.io.util.SeekableByteChannelBinaryWriter;

public final class BufferedFileAccess {

	private static final int SIZE_2_MB = 2 << 20;

	private static ByteBuffer createByteBuffer() {
		return ByteBuffer.allocate(SIZE_2_MB).order(ByteOrder.LITTLE_ENDIAN);
	}

	private FileAccessCache fileCache;
	private Path path;

	private ByteBuffer readBuffer;
	private BinaryReader reader;

	private ByteBuffer writeBuffer;
	private BinaryWriter writer;

	public BufferedFileAccess() {
	}

	public boolean isOpen() {
		return getPath() != null;
	}

	public void open(Path path, EnumSet<StandardOpenOption> openOptions) throws IOException {
		if (this.fileCache != null) {
			if (this.fileCache.getFile().equals(path)) {
				return;
			}
			close();
		}
		this.path = path;
		this.fileCache = new FileAccessCache(60000, path, openOptions);
	}

	public void close() throws IOException {
		if (this.fileCache == null) {
			return;
		}

		this.path = null;

		final var exceptions = new ArrayList<Throwable>();

		if (this.reader != null) {
			try {
				this.reader.close();
				this.reader = null;
				this.readBuffer = null;
			} catch (final Throwable e) { // ignore
				exceptions.add(e);
			}
		}
		if (this.writer != null) {
			try {
				if (this.writer.isOpen()) {
					this.writer.flush();
				}
			} catch (final Throwable e) {
				exceptions.add(e);
			}
			try {
				this.writer.close();
				this.writer = null;
				this.writeBuffer = null;
			} catch (final Throwable e) { // ignore
				exceptions.add(e);
			}
		}

		try {
			this.fileCache.dispose();
		} catch (final Throwable e) {
			exceptions.add(e);
		} finally {
			this.fileCache = null;

		}

		if (!exceptions.isEmpty()) {
			final var exception = new IOException();
			for (final var suppressed : exceptions) {
				exception.addSuppressed(suppressed);
			}
			throw exception;
		}
	}

	public Path getPath() {
		return this.path;
	}

	public BinaryReader getFileReader() throws IOException {
		if (this.fileCache == null) {
			throw new IllegalStateException("No file open");
		}

		if (this.readBuffer == null) {
			this.readBuffer = createByteBuffer();
		}

		if (this.reader == null || !this.reader.isOpen()) {
			// for some reason it is really really expensive to build those
			this.reader = new SeekableByteChannelBinaryReader(this.fileCache.getFileAccess(), this.readBuffer, false);
		}

		final BinaryReader delegate = new BinaryReaderDelegate(this.reader) {
			@Override
			public void close() throws IOException {
				if (isOpen()) {
					BufferedFileAccess.this.fileCache.startExpiring();
				}
				super.close();
			}
		};
		return delegate;
	}

	public BinaryWriter getFileWriter() throws IOException {
		if (this.fileCache == null) {
			throw new IllegalStateException("No file open");
		}

		if (this.writeBuffer == null) {
			this.writeBuffer = createByteBuffer();
		}

		this.fileCache.getFileAccess();
		if (this.writer == null || !this.writer.isOpen()) {
			// for some reason it is really really expensive to build those
			this.writer = new SeekableByteChannelBinaryWriter(this.fileCache.getFileAccess(), this.writeBuffer, false);
		}

		final BinaryWriter delegate = new BinaryWriterDelegate(this.writer) {
			@Override
			public void close() throws IOException {
				if (isOpen()) {
					BufferedFileAccess.this.fileCache.startExpiring();
				}
				super.close();
			}
		};
		return delegate;
	}

}
