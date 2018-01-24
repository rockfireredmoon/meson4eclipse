/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.icemoon.cdt.meson.core.corebuild.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.icemoon.cdt.meson.core.corebuild.internal.messages"; //$NON-NLS-1$
	public static String MesonBuildConfiguration_2;
	public static String MesonBuildConfiguration_3;
	public static String MesonBuildConfiguration_4;
	public static String MesonBuildConfiguration_Building;
	public static String MesonBuildConfiguration_BuildingIn;
	public static String MesonBuildConfiguration_BuildingComplete;
	public static String MesonBuildConfiguration_Cleaning;
	public static String MesonBuildConfiguration_NotFound;
	public static String MesonBuildConfiguration_NoToolchainFile;
	public static String MesonBuildConfiguration_ProcCompCmds;
	public static String MesonBuildConfiguration_ProcCompJson;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
