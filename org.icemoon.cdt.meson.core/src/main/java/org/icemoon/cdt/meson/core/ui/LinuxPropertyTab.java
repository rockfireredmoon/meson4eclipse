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
package org.icemoon.cdt.meson.core.ui;

import java.util.EnumSet;

import org.icemoon.cdt.meson.core.internal.MesonBackend;
import org.icemoon.cdt.meson.core.internal.settings.MesonPreferences;
import org.icemoon.cdt.meson.core.internal.settings.LinuxPreferences;

/**
 * UI to control host Linux specific project properties and preferences for
 * meson. This tab is responsible for storing its values.
 *
 * @author Martin Weber @author Emerald Icemoon
 */
public class LinuxPropertyTab extends AbstractOsPropertyTab<LinuxPreferences> {

	private static final EnumSet<MesonBackend> generators = EnumSet.of(MesonBackend.NINJA, MesonBackend.XCODE);

	/*-
	 * @see org.icemoon.cdt.meson.core.ui.AbstractOsPropertyTab#getOsPreferences(org.icemoon.cdt.meson.core.internal.MesonPreferences)
	 */
	@Override
	protected LinuxPreferences getOsPreferences(MesonPreferences prefs) {
		return prefs.getLinuxPreferences();
	}

	@Override
	protected EnumSet<MesonBackend> getAvailableGenerators() {
		return LinuxPropertyTab.generators;
	}

}
