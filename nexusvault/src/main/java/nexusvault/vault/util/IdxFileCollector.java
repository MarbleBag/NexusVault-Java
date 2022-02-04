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

package nexusvault.vault.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import nexusvault.vault.IdxEntry.IdxFileLink;
import nexusvault.vault.util.IdxDirectoryTraverser.IdxEntryVisitor;

/**
 * A utility class that provides a mean to collect a number of {@link IdxFileLink} for a given {@link Predicate predicate}
 * <p>
 * If a {@link IdxFileLink} fulfills a {@link Predicate predicate}, the file will be added to the result, which can be retrieved with
 * {@link #getAndClearResult()}
 * <p>
 * This collector implements {@link IdxEntryVisitor} interface and thus can be used in combination with {@link IdxDirectoryTraverser}
 *
 * @see ReportingIdxFileCollector
 */
public class IdxFileCollector implements IdxEntryVisitor {

	protected final int maxNumberOfResults;
	protected final List<IdxFileLink> result;
	protected final Predicate<IdxFileLink> predicate;

	public IdxFileCollector(Predicate<IdxFileLink> predicate) {
		this(predicate, -1);
	}

	public IdxFileCollector(Predicate<IdxFileLink> predicate, int maxNumberOfResults) {
		if (predicate == null) {
			throw new IllegalArgumentException("'predicate' must not be null");
		}
		this.predicate = predicate;
		this.result = new LinkedList<>();
		this.maxNumberOfResults = maxNumberOfResults;
	}

	@Override
	public EntryFilterResult visitFile(IdxFileLink file) {
		if (this.predicate.test(file)) {
			this.result.add(file);
			if (0 <= this.maxNumberOfResults && this.maxNumberOfResults <= this.result.size()) {
				return EntryFilterResult.TERMINATE;
			}
		}
		return EntryFilterResult.CONTINUE;
	}

	/**
	 * Returns the results and clears the internal memory. <br>
	 * Subsequent calls to this method will return an empty list.
	 *
	 * @return the collected results
	 */
	public List<IdxFileLink> getAndClearResult() {
		final List<IdxFileLink> result = new ArrayList<>(this.result);
		this.result.clear();
		return result;
	}

}
