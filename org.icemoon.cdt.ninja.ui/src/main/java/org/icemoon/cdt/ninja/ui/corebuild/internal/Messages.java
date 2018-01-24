/*******************************************************************************
 * Copyright (c) 2010, 2014 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.corebuild.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String NewNinjaProjectWizard_Description;
	public static String NewNinjaProjectWizard_PageTitle;
	public static String NewNinjaProjectWizard_WindowTitle;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
