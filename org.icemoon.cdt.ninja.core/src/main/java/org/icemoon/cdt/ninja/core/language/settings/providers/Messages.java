/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.core.language.settings.providers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.icemoon.cdt.ninja.core.language.settings.providers.messages"; //$NON-NLS-1$
	public static String NinjaBuildConfiguration_Building;
	public static String NinjaBuildConfiguration_Failed;
	public static String NinjaBuildConfiguration_BuildingIn;
	public static String NinjaBuildConfiguration_BuildingComplete;
	public static String NinjaBuildConfiguration_Cleaning;
	public static String NinjaBuildConfiguration_NotFound;
	public static String NinjaBuildConfiguration_NoToolchainFile;
	public static String NinjaBuildConfiguration_ProcCompCmds;
	public static String NinjaBuildConfiguration_ProcCompJson;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
