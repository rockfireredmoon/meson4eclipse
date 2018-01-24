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

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.icemoon.cdt.meson.core.MesonProjectOptionType;
import org.icemoon.cdt.meson.core.ProjectOption;

/**
 * Responsible for serialization/de-serialization of ProjectOption objects.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class ProjectOptionSerializer implements StorageSerializer<ProjectOption> {
	/**  */
	private static final String ELEM_DEFINE = "def";
	/**  */
	private static final String ATTR_MESONVAR_TYPE = "type";
	/**  */
	private static final String ATTR_MESONVAR_VALUE = "val";
	/**  */
	private static final String ATTR_NAME = "name";

	public void toStorage(ICStorageElement parent, ProjectOption item) {
		ICStorageElement elem = parent.createChild(ELEM_DEFINE);
		elem.setAttribute(ATTR_NAME, item.getName());
		elem.setAttribute(ATTR_MESONVAR_TYPE, item.getType().name());
		elem.setAttribute(ATTR_MESONVAR_VALUE, item.getValue());
	}

	/*-
	 * @see StorageSerializer#fromStorage(org.eclipse.cdt.core.settings.model.ICStorageElement)
	 */
	@Override
	public ProjectOption fromStorage(ICStorageElement item) {
		if (!ELEM_DEFINE.equals(item.getName()))
			return null; // item is not an element representing a meson define
		String nameVal = item.getAttribute(ATTR_NAME);
		String typeVal = item.getAttribute(ATTR_MESONVAR_TYPE);
		String valueVal = item.getAttribute(ATTR_MESONVAR_VALUE);
		if (nameVal != null && typeVal != null && valueVal != null) {
			try {
				final MesonProjectOptionType type = MesonProjectOptionType.valueOf(typeVal);
				return new ProjectOption(nameVal, type, valueVal);
			} catch (IllegalArgumentException ex) {
				// illegal meson variable type, ignore
			}
		}
		return null;
	}
}