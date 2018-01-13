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
package org.icemoon.cdt.meson.core.internal.settings;

import org.icemoon.cdt.meson.core.internal.MesonBackend;

/**
 * Preferences that override/augment the generic properties when running under
 * Windows.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class WindowsPreferences extends AbstractOsPreferences {

	private static final String ELEM_OS = "win32";

	/** Overridden to set a sensible generator. */
	public void reset() {
		super.reset();
		setBackend(MesonBackend.VS);
	}

	/**
	 * @return the String "win32".
	 */
	protected String getStorageElementName() {
		return ELEM_OS;
	}
}
