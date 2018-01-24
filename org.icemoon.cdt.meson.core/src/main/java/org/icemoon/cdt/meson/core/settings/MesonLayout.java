
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
public enum MesonLayout {

	DEFAULT, MIRROR, FLAT;

	public static String[] names() {
		String[] n = new String[values().length];
		int i = 0;
		for (MesonLayout l : values()) {
			n[i++] = l.name();
		}
		return n;
	}

	public String getName() {
		switch (this) {
		case DEFAULT:
			return "Default";
		case MIRROR:
			return "Mirror";
		default:
			return "Flat";
		}
	}
}
