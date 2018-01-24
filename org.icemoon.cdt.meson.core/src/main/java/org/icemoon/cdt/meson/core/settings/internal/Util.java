/*******************************************************************************
 * Copyright (c) 2014-2017 Martin Weber.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Martin Weber - Initial implementation
 *      Emerald Icemoon - Ported to Meson
 *******************************************************************************/
package org.icemoon.cdt.meson.core.settings.internal;

import java.util.Collection;

import org.eclipse.cdt.core.settings.model.ICStorageElement;

/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class Util {

	/**
	 * Nothing to instantiate here, just static methods.
	 */
	private Util() {
	}

	/**
	 * Converts a collection of {@code T} objects to {@link ICStorageElement}s.
	 *
	 * @param targetCollectionStorageName
	 *            the name for the ICStorageElement representing the collection. It
	 *            will be created.
	 * @param parent
	 *            the parent element, must not be {@code null}.
	 * @param itemSerializer
	 *            the object that converts a collection element.
	 * @param source
	 *            the collection to convert, must not be {@code null}.
	 */
	public static <E> void serializeCollection(String targetCollectionStorageName, ICStorageElement parent,
			StorageSerializer<E> itemSerializer, Collection<E> source) {
		ICStorageElement[] existingColls = parent.getChildrenByName(targetCollectionStorageName);

		ICStorageElement pColl;
		if (existingColls.length > 0) {
			pColl = existingColls[0];
			if (source.isEmpty()) {
				// remove element if collection is empty
				parent.removeChild(pColl);
				return;
			}
		} else {
			pColl = parent.createChild(targetCollectionStorageName);
		}
		// to avoid duplicates, since we do not track additions/removals to lists..
		pColl.clear();

		// serialize collection elements
		for (E elem : source) {
			itemSerializer.toStorage(pColl, elem);
		}
	}

	/**
	 * Converts an {@link ICStorageElement} to a collection of {@code T} objects.
	 *
	 * @param target
	 *            the collection to store the converted objects in, must not be
	 *            {@code null}.
	 * @param itemSerializer
	 *            the object that converts a collection element to an Object.
	 * @param sourceParent
	 *            the parent element of the collection to read, must not be
	 *            {@code null}.
	 */
	public static <E> void deserializeCollection(Collection<E> target, StorageSerializer<E> itemSerializer,
			ICStorageElement sourceParent) {
		for (ICStorageElement elem : sourceParent.getChildren()) {
			E item = itemSerializer.fromStorage(elem);
			if (item != null)
				target.add(item);
		}
	}

}
