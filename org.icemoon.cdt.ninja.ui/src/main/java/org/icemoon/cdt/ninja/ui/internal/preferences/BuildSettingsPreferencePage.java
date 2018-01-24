package org.icemoon.cdt.ninja.ui.internal.preferences;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractPrefPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;

/**
 * Preference page for Build Settings.
 *
 */
public class BuildSettingsPreferencePage extends AbstractPrefPage {

	@Override
	protected String getHeader() {
		return NinjaFilePreferencesMessages.getString("BuildPreferencePage.description"); //$NON-NLS-1$
	}

	/*
	 * All affected settings are stored in preferences. Tabs are responsible for
	 * saving, after OK signal. No need to affect Project Description somehow.
	 */
	@Override
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		return true;
	}

	@Override
	public ICResourceDescription getResDesc() {
		return null;
	}

	@Override
	protected boolean isSingle() {
		return false;
	}
}
