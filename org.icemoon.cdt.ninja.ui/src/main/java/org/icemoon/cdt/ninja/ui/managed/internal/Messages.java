/*******************************************************************************
 * Copyright (c) 2010, 2014 Andrew Gvozdev and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *     IBM Corporation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.managed.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String AbstractCWizard_0;
	public static String StdBuildWizard_0;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
