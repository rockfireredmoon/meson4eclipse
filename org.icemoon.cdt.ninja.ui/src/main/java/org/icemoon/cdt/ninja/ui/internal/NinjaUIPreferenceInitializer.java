/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.icemoon.cdt.ninja.ui.internal.preferences.NinjaFileEditorPreferenceConstants;
import org.icemoon.cdt.ninja.ui.internal.preferences.NinjaFileEditorPreferencePage;
import org.icemoon.cdt.ninja.ui.internal.preferences.NinjaPreferencePage;

public class NinjaUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = NinjaUIPlugin.getDefault().getPreferenceStore();
		NinjaPreferencePage.initDefaults(store);
		NinjaFileEditorPreferenceConstants.initializeDefaultValues(store);
		NinjaFileEditorPreferencePage.initDefaults(store);
	}

}
