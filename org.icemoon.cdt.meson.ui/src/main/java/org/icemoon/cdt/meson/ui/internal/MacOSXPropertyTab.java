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
package org.icemoon.cdt.meson.ui.internal;

import java.util.EnumSet;

import org.icemoon.cdt.meson.core.MesonBackend;
import org.icemoon.cdt.meson.core.settings.MacOSXPreferences;
import org.icemoon.cdt.meson.core.settings.MesonPreferences;

/**
 * UI to control host Linux specific project properties and preferences for
 * meson. This tab is responsible for storing its values.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MacOSXPropertyTab extends AbstractOsPropertyTab<MacOSXPreferences> {

	private static final EnumSet<MesonBackend> generators = EnumSet.of(MesonBackend.NINJA, MesonBackend.XCODE);

	@Override
	protected EnumSet<MesonBackend> getAvailableGenerators() {
		return MacOSXPropertyTab.generators;
	}

	/*-
	 * @see org.icemoon.cdt.meson.ui.AbstractOsPropertyTab#getOsPreferences(org.icemoon.cdt.meson.core.internal.MesonPreferences)
	 */
	@Override
	protected MacOSXPreferences getOsPreferences(MesonPreferences prefs) {
		return prefs.getMacOSXPreferences();
	}

}
