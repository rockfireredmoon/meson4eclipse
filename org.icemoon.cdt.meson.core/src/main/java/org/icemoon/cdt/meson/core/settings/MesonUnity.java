/*******************************************************************************
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Emerald Icemoon - Initial implementation
 *******************************************************************************/

package org.icemoon.cdt.meson.core.settings;

/**
 * @author Emerald Icemoon
 */
public enum MesonUnity {
	DEFAULT, ON, OFF, SUBPROJECTS;

	public static String[] names() {
		String[] n = new String[values().length];
		int i = 0;
		for (MesonUnity l : values()) {
			n[i++] = l.name();
		}
		return n;
	}

	public String getName() {
		switch (this) {
		case ON:
			return "On";
		case SUBPROJECTS:
			return "Subprojects";
		case OFF:
			return "Off";
		default:
			return "Default";
		}
	}
}
