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

package nexusvault.format.m3.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

final class Class2ObjectLookup<S> {
	private final Map<Class<?>, S> innitialMap;
	private final Map<Class<?>, S> cache;
	private final Supplier<S> defaultValueSupplier;

	public Class2ObjectLookup(Supplier<S> defaultValueSupplier) {
		this.innitialMap = new HashMap<>();
		this.cache = new HashMap<>();
		this.defaultValueSupplier = defaultValueSupplier;
	}

	public void setLookUp(Class<?> keyClass, S data) {
		this.innitialMap.put(keyClass, data);
		this.cache.put(keyClass, data);
	}

	public void clearCache() {
		this.cache.clear();
	}

	public S getWithoutLookUp(Class<?> keyClass) {
		return this.innitialMap.get(keyClass);
	}

	public S getLookUp(Class<?> keyClass) {
		S data = this.cache.get(keyClass);
		if (data != null) {
			return data;
		}

		data = searchLookUp(keyClass);
		if (data == null) {
			return null;
			// data = defaultValueSupplier.get();
		}

		this.cache.put(keyClass, data);
		return data;
	}

	private S searchLookUp(Class<?> keyClass) {
		S data = getWithoutLookUp(keyClass);
		if (data == null) {
			final Class<?> key = findLookUpKey(keyClass);
			data = getWithoutLookUp(key);
		}
		return data;
	}

	private Class<?> findLookUpKey(Class<?> keyClass) {
		final List<Class<?>> assignableKeys = getAssignableKeys(keyClass);
		if (assignableKeys.isEmpty()) {
			return null;
		}

		if (assignableKeys.size() == 1) {
			return assignableKeys.get(0);
		}

		return checkSuperClasses(assignableKeys, keyClass);
	}

	private List<Class<?>> getAssignableKeys(Class<?> keyClass) {
		final List<Class<?>> assignableKeys = new LinkedList<>();
		for (final Class<?> key : this.innitialMap.keySet()) {
			if (key.isAssignableFrom(keyClass)) {
				assignableKeys.add(key);
			}
		}
		return assignableKeys;
	}

	private Class<?> checkSuperClasses(List<Class<?>> assignableKeys, Class<?> keyClass) {
		if (assignableKeys.isEmpty()) {
			return null;
		}

		List<Class<?>> keyClassSupers = getSuperClasses(keyClass);
		assignableKeys = new ArrayList<>(assignableKeys);

		while (true) {
			final Iterator<Class<?>> assignableKeyItr = assignableKeys.iterator();

			while (assignableKeyItr.hasNext()) {
				final Class<?> assignableKey = assignableKeyItr.next();
				boolean isSuperClassAssignable = false;

				for (final Class<?> keyClassSuper : keyClassSupers) {
					if (assignableKey.equals(keyClassSuper)) {
						return assignableKey;
					} else if (assignableKey.isAssignableFrom(keyClassSuper)) {
						isSuperClassAssignable = true;
						break;
					}
				}

				if (!isSuperClassAssignable) {
					assignableKeyItr.remove();
				}
			}

			if (assignableKeys.isEmpty()) {
				throw new IllegalStateException(String.format("A super-class of %1$s became a non-super-class of %1$s", keyClass));
			} else if (assignableKeys.size() == 1) {
				return assignableKeys.get(0);
			}

			keyClassSupers = getSuperClasses(keyClassSupers);
		}
	}

	private List<Class<?>> getSuperClasses(Class<?> clazz) {
		final List<Class<?>> list = new LinkedList<>();
		if (clazz.getSuperclass() != null) {
			list.add(clazz.getSuperclass());
		}
		list.addAll(Arrays.asList(clazz.getInterfaces()));
		return list;
	}

	private List<Class<?>> getSuperClasses(List<Class<?>> clazz) {
		final List<Class<?>> list = new LinkedList<>();
		for (final Class<?> c : clazz) {
			list.addAll(getSuperClasses(c));
		}
		return new ArrayList<>(list);
	}

}