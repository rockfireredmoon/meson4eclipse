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

	public static String NinjaBuildTab_BuildCommand;
	public static String NinjaBuildTab_CleanCommand;
	public static String NinjaBuildTab_Cmake;
	public static String NinjaBuildTab_NinjaArgs;
	public static String NinjaBuildTab_Generator;
	public static String NinjaBuildTab_Ninja;
	public static String NinjaBuildTab_UseCustom;
	public static String NinjaBuildTab_NoneAvailable;
	public static String NinjaBuildTab_Settings;
	public static String NinjaBuildTab_Toolchain;
	public static String NinjaBuildTab_UnixMakefiles;
	public static String NinjaPreferencePage_Add;
	public static String NinjaPreferencePage_ConfirmRemoveDesc;
	public static String NinjaPreferencePage_ConfirmRemoveTitle;
	public static String NinjaPreferencePage_Files;
	public static String NinjaPreferencePage_Path;
	public static String NinjaPreferencePage_Remove;
	public static String NinjaPreferencePage_Toolchain;
	public static String NinjaPropertyPage_FailedToStartNinjaGui_Body;
	public static String NinjaPropertyPage_FailedToStartNinjaGui_Title;
	public static String NinjaPropertyPage_LaunchNinjaGui;

	public static String NewNinjaProjectWizard_Description;
	public static String NewNinjaProjectWizard_PageTitle;
	public static String NewNinjaProjectWizard_WindowTitle;

	public static String NewNinjaToolChainFilePage_Browse;
	public static String NewNinjaToolChainFilePage_NoPath;
	public static String NewNinjaToolChainFilePage_Path;
	public static String NewNinjaToolChainFilePage_Select;
	public static String NewNinjaToolChainFilePage_Title;
	public static String NewNinjaToolChainFilePage_Toolchain;

	static {
		// initialize resource bundle
		NLS.initializeMessages("org.icemoon.cdt.ninja.ui.internal.preferences.messages", Messages.class); //$NON-NLS-1$
	}

	private Messages() {
	}
}
