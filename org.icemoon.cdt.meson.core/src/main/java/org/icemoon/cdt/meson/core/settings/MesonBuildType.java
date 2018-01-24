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
public enum MesonBuildType {

	DEFAULT, PLAIN, DEBUG, DEBUGOPTIMIZED, RELEASE, MINSIZE;

	public static String[] names() {
		String[] n = new String[values().length];
		int i = 0;
		for (MesonBuildType l : values()) {
			n[i++] = l.name();
		}
		return n;
	}

	public String getName() {
		switch (this) {
		case PLAIN:
			return "Plain";
		case DEBUGOPTIMIZED:
			return "Debug Optimized";
		case RELEASE:
			return "Release";
		case MINSIZE:
			return "Minimum Size";
		case DEBUG:
			return "Debug";
		default:
			return "Default";
		}
	}
}
