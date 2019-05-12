/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String NinjaPropertyPage_BuildCommand;
	public static String NinjaPropertyPage_CleanCommand;
	public static String NinjaPropertyPage_UseCustom;

	public static String NewNinjaProjectWizard_Description;
	public static String NewNinjaProjectWizard_PageTitle;
	public static String NewNinjaProjectWizard_WindowTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages("org.icemoon.cdt.ninja.ui.internal.preferences.messages", Messages.class); //$NON-NLS-1$
	}

	private Messages() {
	}
}
