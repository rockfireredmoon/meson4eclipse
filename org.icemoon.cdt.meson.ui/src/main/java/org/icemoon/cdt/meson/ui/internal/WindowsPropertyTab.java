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
import org.icemoon.cdt.meson.core.settings.MesonPreferences;
import org.icemoon.cdt.meson.core.settings.WindowsPreferences;

/**
 * UI to control host Windows specific project properties and preferences for
 * meson. This tab is responsible for storing its values.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class WindowsPropertyTab extends AbstractOsPropertyTab<WindowsPreferences> {

	private static final EnumSet<MesonBackend> generators = EnumSet.of(MesonBackend.NINJA, MesonBackend.XCODE,
			MesonBackend.VS, MesonBackend.VS2010, MesonBackend.VS2015, MesonBackend.VS2017);

	@Override
	protected EnumSet<MesonBackend> getAvailableGenerators() {
		return WindowsPropertyTab.generators;
	}

	/*-
	 * @see org.icemoon.cdt.meson.ui.AbstractOsPropertyTab#getOsPreferences(org.icemoon.cdt.meson.core.internal.MesonPreferences)
	 */
	@Override
	protected WindowsPreferences getOsPreferences(MesonPreferences prefs) {
		return prefs.getWindowsPreferences();
	}

}
