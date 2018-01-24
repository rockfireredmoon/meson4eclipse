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
package org.icemoon.cdt.meson.ui.internal;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.ProjectOption;
import org.icemoon.cdt.meson.core.settings.ConfigurationManager;
import org.icemoon.cdt.meson.core.settings.MesonPreferences;

/**
 * UI to control general project properties for meson. This tab is responsible
 * for storing its values.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonProjectOptionsTab extends QuirklessAbstractCPropertyTab {

	/**  */
	private static final ILog log = Activator.getDefault().getLog();

	/** the table showing the meson project options */
	private ProjectOptionsViewer projectOptions;

	// This page can be displayed for project
	@Override
	public boolean canBeVisible() {
		return page.isForProject();
	}

	@Override
	protected void configSelectionChanged(ICResourceDescription lastConfig, ICResourceDescription newConfig) {

		if (newConfig == null)
			return;
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
		} else {
			setAllVisible(true, null);

			ICConfigurationDescription cfgd = newConfig.getConfiguration();
			final ConfigurationManager configMgr = ConfigurationManager.getInstance();
			try {
				MesonPreferences prefs = configMgr.getOrLoad(cfgd);
				updateDisplay(prefs);
			} catch (CoreException ex) {
				log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
			}
		}
	}

	@Override
	protected void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));
		GridData ld = (GridData) usercomp.getLayoutData();
		ld.verticalAlignment = GridData.FILL;
		ld.grabExcessHorizontalSpace = true;
		ld.grabExcessVerticalSpace = true;
		ld.horizontalAlignment = GridData.FILL;
		final ICResourceDescription resDesc = getResDesc();
		projectOptions = new ProjectOptionsViewer(usercomp, resDesc == null ? null : resDesc.getConfiguration());
	}

	/**
	 * Invoked when project configuration changes?? At least when apply button is
	 * pressed.
	 *
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription,
	 *      org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		ICConfigurationDescription srcCfg = src.getConfiguration();
		ICConfigurationDescription dstCfg = dst.getConfiguration();
		final ConfigurationManager configMgr = ConfigurationManager.getInstance();

		try {
			MesonPreferences srcPrefs = configMgr.getOrLoad(srcCfg);
			MesonPreferences dstPrefs = configMgr.getOrCreate(dstCfg);
			if (srcPrefs != dstPrefs) {

				final List<ProjectOption> defines = dstPrefs.getProjectOptions();
				defines.clear();
				for (ProjectOption def : srcPrefs.getProjectOptions()) {
					defines.add(def.clone());
				}

			}
		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
		}
	}

	/*-
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		final ICResourceDescription resDesc = getResDesc();
		if (resDesc == null)
			return;
		final ICConfigurationDescription cfgd = resDesc.getConfiguration();
		final MesonPreferences prefs = ConfigurationManager.getInstance().get(cfgd);
		prefs.reset();
		updateDisplay(prefs);
	}

	@Override
	protected void performOK() {
		final ICResourceDescription resDesc = getResDesc();
		if (resDesc == null)
			return;
		final ICConfigurationDescription cfgd = resDesc.getConfiguration();
		if (cfgd instanceof ICMultiConfigDescription) {
			// this tab does not support editing of multiple configurations
			return;
		}
		try {
			// NB: defines & undefines are modified by the widget listeners directly
			MesonPreferences prefs = ConfigurationManager.getInstance().get(cfgd);

			// save as project settings..
			ICStorageElement storage = cfgd.getStorage(MesonPreferences.CFG_STORAGE_ID, true);
			prefs.saveToStorage(storage);

		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
		}
	}

	/*-
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateButtons()
	 */
	@Override
	protected void updateButtons() {
		// never called from superclass, but abstract :-)
	}

	/**
	 * Updates displayed values according to the preferences edited by this tab.
	 *
	 * @param mesonPreferences
	 *            the MesonPreferences to display, never <code>null</code>
	 */
	private void updateDisplay(MesonPreferences mesonPreferences) {
		projectOptions.setInput(mesonPreferences.getProjectOptions());
	}

}
