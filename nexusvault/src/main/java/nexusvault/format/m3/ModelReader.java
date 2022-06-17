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

package nexusvault.format.m3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import kreed.io.util.ByteArrayUtil;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.InMemoryModel;
import nexusvault.format.m3.impl.ReferenceUpdater;
import nexusvault.format.m3.struct.StructM3Header;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class ModelReader {
	private ModelReader() {
	}

	private static final int VERSION = 100;

	public static Model read(byte[] data) {
		return read(data, ByteOrder.LITTLE_ENDIAN);
	}

	private static Model read(byte[] data, ByteOrder order) {
		final var signature = ByteArrayUtil.getInt32(data, 0, order);
		if (signature != StructM3Header.SIGNATURE) {
			throw new SignatureMismatchException("m3", StructM3Header.SIGNATURE, signature);
		}

		final var version = ByteArrayUtil.getInt32(data, 4, order);
		if (version != VERSION) {
			throw new VersionMismatchException("m3", VERSION, version);
		}

		final var tracker = new BytePositionTracker(0, data.length, ByteBuffer.wrap(data).order(order));
		final var entry = ReferenceUpdater.update(tracker, StructM3Header.class);
		return new InMemoryModel(entry, tracker);
	}

}
