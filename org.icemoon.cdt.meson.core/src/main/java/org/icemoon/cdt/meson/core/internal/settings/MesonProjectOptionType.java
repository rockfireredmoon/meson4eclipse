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

/**
 * The type identifier of a meson variable.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public enum MesonProjectOptionType {
	/** Boolean ON/OFF checkbox */
	BOOL {
		@Override
		public String getCmakeArg() {
			return "BOOL";
		}
	},
	/** File chooser dialog */
	FILEPATH {
		@Override
		public String getCmakeArg() {
			return "FILEPATH";
		}
	},
	/** Directory chooser dialog */
	PATH {
		@Override
		public String getCmakeArg() {
			return "PATH";
		}
	},
	/** Arbitrary string */
	STRING {
		@Override
		public String getCmakeArg() {
			return "STRING";
		}
	},
	/** No GUI entry (used for persistent variables) */
	INTERNAL {
		@Override
		public String getCmakeArg() {
			return "INTERNAL";
		}
	};

	/**
	 * Gets the type as a valid commandline argument for meson.
	 */
	public abstract String getCmakeArg();
}