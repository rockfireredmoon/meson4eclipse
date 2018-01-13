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
package org.icemoon.cdt.meson.core.ui;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.NewFolderDialog;
import org.icemoon.cdt.meson.core.CdtPlugin;
import org.icemoon.cdt.meson.core.internal.settings.ConfigurationManager;
import org.icemoon.cdt.meson.core.internal.settings.MesonBuildType;
import org.icemoon.cdt.meson.core.internal.settings.MesonDefaultLibrary;
import org.icemoon.cdt.meson.core.internal.settings.MesonPreferences;

/**
 * UI to control general project properties for meson. This tab is responsible
 * for storing its values.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonPropertyTab extends QuirklessAbstractCPropertyTab {

	/**  */
	private static final ILog log = CdtPlugin.getDefault().getLog();

	// Widgets
	private Combo b_warnLevel;
	private Combo b_defaultLibrary;
	private Combo b_buildType;
	private Button b_errorLogs;
	private Button b_werror;
	/** build directory */
	private Text t_outputFolder;
	private Button b_browseOutputFolder;
	private Button b_createOutputFolder;
	/** variables in output folder text field */
	private Button b_cmdVariables;

	/**
	 * the preferences associated with our configurations to manage. Initialized in
	 * {@link #updateData}
	 */
	private MesonPreferences[] prefs;

	/** shared listener for check-boxes */
	private TriStateButtonListener tsl = new TriStateButtonListener();

	@Override
	protected void createControls(final Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
		// usercomp.setBackground(BACKGROUND_FOR_USER_VAR);

		// output folder group
		{
			Group gr = WidgetHelper.createGroup(usercomp, SWT.FILL, 2,
					"Build output location (relative to project root)", 2);

			setupLabel(gr, "F&older", 1, SWT.BEGINNING);

			t_outputFolder = setupText(gr, 1, GridData.FILL_HORIZONTAL);

			// "Browse", "Create" dialog launcher buttons...
			Composite buttonBar = new Composite(gr, SWT.NONE);
			{
				buttonBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 3, 1));
				GridLayout layout = new GridLayout(3, false);
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				buttonBar.setLayout(layout);
			}
			b_browseOutputFolder = WidgetHelper.createButton(buttonBar, "B&rowse...", true);
			b_browseOutputFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(
							b_browseOutputFolder.getShell(), false, page.getProject(), IResource.FOLDER);
					dialog.setTitle("Select output folder");
					dialog.open();
					IFolder folder = (IFolder) dialog.getFirstResult();
					if (folder != null) {
						// insert selected resource name
						t_outputFolder.setText(folder.getProjectRelativePath().toPortableString());
					}
				}
			});
			b_createOutputFolder = WidgetHelper.createButton(buttonBar, "&Create...", true);
			b_createOutputFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					NewFolderDialog dialog = new NewFolderDialog(parent.getShell(), page.getProject());
					if (dialog.open() == Window.OK) {
						IFolder f = (IFolder) dialog.getFirstResult();
						t_outputFolder.setText(f.getProjectRelativePath().toPortableString());
					}
				}
			});

			b_cmdVariables = WidgetHelper.createButton(buttonBar, "Insert &Variable...", true);
			b_cmdVariables.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final ICResourceDescription resDesc = getResDesc();
					if (resDesc == null)
						return;
					ICConfigurationDescription cfgd = resDesc.getConfiguration();
					String text = AbstractCPropertyTab.getVariableDialog(t_outputFolder.getShell(), cfgd);
					if (text != null) {
						t_outputFolder.insert(text);
					}
				}
			});
		}
		// Meson options group...
		{
			Group gr = WidgetHelper.createGroup(usercomp, SWT.FILL, 2, "Meson commandline options", 2);

			setupLabel(gr, "Warnings Level \t(--warnlevel)", 1, SWT.BEGINNING);
			b_warnLevel = WidgetHelper.createCombo(gr, SWT.BEGINNING, 2, "1", true, "1", "2", "3");
			b_warnLevel.addListener(SWT.Selection, tsl);

			setupLabel(gr, "Build Type \t(--buildtype)", 1, SWT.BEGINNING);
			List<String> types = new LinkedList<>(Arrays.asList("Default"));
			types.addAll(Arrays.asList(MesonBuildType.names()));
			b_buildType = WidgetHelper.createCombo(gr, SWT.BEGINNING, 2, "1", true, types.toArray(new String[0]));
			b_buildType.addListener(SWT.Selection, tsl);

			setupLabel(gr, "Default Library \t(--default-library)", 1, SWT.BEGINNING);
			b_defaultLibrary = WidgetHelper.createCombo(gr, SWT.BEGINNING, 2, "1", true, MesonDefaultLibrary.names());
			b_defaultLibrary.addListener(SWT.Selection, tsl);

			b_errorLogs = WidgetHelper.createCheckbox(gr, SWT.BEGINNING, 2,
					"Print the logs from failing tests. \t(--errorlogs)");
			b_errorLogs.addListener(SWT.Selection, tsl);
			
			b_werror = WidgetHelper.createCheckbox(gr, SWT.BEGINNING, 2, "Treat warnings as errors \t\t(--werror)");
			b_werror.addListener(SWT.Selection, tsl);

		} // Meson options group

	}

	@Override
	protected void configSelectionChanged(ICResourceDescription lastConfig, ICResourceDescription newConfig) {
		if (lastConfig != null) {
			saveToModel();
		}
		if (newConfig == null)
			return;

		ICConfigurationDescription cfgd = newConfig.getConfiguration();
		final ConfigurationManager configMgr = ConfigurationManager.getInstance();
		try {
			if (cfgd instanceof ICMultiConfigDescription) {
				// we are editing multiple configurations...
				ICConfigurationDescription[] cfgs = (ICConfigurationDescription[]) ((ICMultiConfigDescription) cfgd)
						.getItems();

				prefs = new MesonPreferences[cfgs.length];
				for (int i = 0; i < cfgs.length; i++) {
					prefs[i] = configMgr.getOrLoad(cfgs[i]);
				}
			} else {
				// we are editing a single configuration...
				prefs = new MesonPreferences[1];
				prefs[0] = configMgr.getOrLoad(cfgd);
			}
		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, CdtPlugin.PLUGIN_ID, null, ex));
		}
		updateDisplay();
	}

	/**
	 * Sets the value of the build folder entry field and whether the user can edit
	 * that input field.
	 *
	 * @param text
	 *            the text to display in the cache-file field
	 */
	private void setBuildFolderEditable(boolean editable, String text) {
		text = editable ? text : " <configurations differ> ";
		t_outputFolder.setText(text == null ? "" : text);
		t_outputFolder.setEditable(editable);
		t_outputFolder.setEnabled(editable);
		b_browseOutputFolder.setEnabled(editable);
		b_createOutputFolder.setEnabled(editable);
		b_cmdVariables.setEnabled(editable);
	}

	/**
	 * Updates displayed values according to the preferences edited by this tab.
	 */
	private void updateDisplay() {
		boolean buildFolderEditable = true;
		boolean warnLevelEditable = true;
		boolean defaultLibraryEditable = true;
		boolean buildTypeEditable = true;

		if (prefs.length > 1) {
			// we are editing multiple configurations...
			/*
			 * make each button tri-state, if its settings are not the same in all
			 * configurations
			 */
			BitSet bs = new BitSet(prefs.length);

			// b_errorLogs...
			bs.clear();
			for (int i = 0; i < prefs.length; i++) {
				bs.set(i, prefs[i].isErrorLogs());
			}
			enterTristateOrToggleMode(b_errorLogs, bs, prefs.length);
			// b_werror...
			bs.clear();
			for (int i = 0; i < prefs.length; i++) {
				bs.set(i, prefs[i].isWarningsAsErrors());
			}
			enterTristateOrToggleMode(b_werror, bs, prefs.length);
			// b_warnLevel
			/*
			 * make b_warnLevel disabled, if its settings are not the same in all
			 * configurations
			 */
			{
				final int wl0 = prefs[0].getWarnLevel();
				for (int i = 1; i < prefs.length; i++) {
					int wl = prefs[i].getWarnLevel();
					if ((wl != wl0)) {
						warnLevelEditable = false;
						break;
					}
				}
			}
			// b_defaultLibrary
			/*
			 * make b_defaultLibrary disabled, if its settings are not the same in all
			 * configurations
			 */
			{
				final MesonDefaultLibrary wl0 = prefs[0].getDefaultLibrary();
				for (int i = 1; i < prefs.length; i++) {
					MesonDefaultLibrary wl = prefs[i].getDefaultLibrary();
					if (!Objects.equals(wl, wl0)) {
						warnLevelEditable = false;
						break;
					}
				}
			}

			// b_defaultLibrary
			/*
			 * make b_defaultLibrary disabled, if its settings are not the same in all
			 * configurations
			 */
			{
				final MesonDefaultLibrary wl0 = prefs[0].getDefaultLibrary();
				for (int i = 1; i < prefs.length; i++) {
					MesonDefaultLibrary wl = prefs[i].getDefaultLibrary();
					if (!Objects.equals(wl, wl0)) {
						defaultLibraryEditable = false;
						break;
					}
				}
			}
			// b_buildType
			/*
			 * make b_build disabled, if its settings are not the same in all configurations
			 */
			{
				final MesonBuildType wl0 = prefs[0].getBuildType();
				for (int i = 1; i < prefs.length; i++) {
					MesonBuildType wl = prefs[i].getBuildType();
					if (!Objects.equals(wl, wl0)) {
						buildTypeEditable = false;
						break;
					}
				}
			}

			/*
			 * make t_outputFolder disabled, if its settings are not the same in all
			 * configurations
			 */
			{
				final String cf0 = prefs[0].getBuildDirectory();
				for (int i = 1; i < prefs.length; i++) {
					String cf = prefs[i].getBuildDirectory();
					if (cf0 != null) {
						if (!cf0.equals(cf)) {
							// configurations differ
							buildFolderEditable = false;
							break;
						}
					} else if (cf != null) {
						// configurations differ
						buildFolderEditable = false;
						break;
					}
				}
			}
		} else {
			// we are editing a single configuration...
			// all buttons are in toggle mode
			MesonPreferences pref = prefs[0];
			int idx = Arrays.asList(b_warnLevel.getItems()).indexOf(String.valueOf(pref.getWarnLevel()));
			if (idx != -1)
				b_warnLevel.select(idx);
			idx = Arrays.asList(b_defaultLibrary.getItems()).indexOf(pref.getDefaultLibrary().name());
			if (idx != -1)
				b_defaultLibrary.select(idx);
			idx = pref.getBuildType() == null ? -1
					: Arrays.asList(b_buildType.getItems()).indexOf(pref.getBuildType().name());
			if (idx != -1)
				b_buildType.select(idx);
			else
				b_buildType.select(0);
			enterToggleMode(b_errorLogs, pref.isStdSplit());
			enterToggleMode(b_werror, pref.isWarningsAsErrors());
		}

		b_warnLevel.setEnabled(warnLevelEditable);
		b_defaultLibrary.setEnabled(defaultLibraryEditable);
		b_buildType.setEnabled(buildTypeEditable);
		String text = prefs[0].getBuildDirectory();
		setBuildFolderEditable(buildFolderEditable, text == null ? "builddir/${ConfigName}" : text);
	}

	/**
	 * Stores displayed values to the preferences edited by this tab.
	 *
	 * @see #updateDisplay()
	 */
	private void saveToModel() {
		if (prefs.length > 1) {
			// we are editing multiple configurations...
			for (int i = 0; i < prefs.length; i++) {
				MesonPreferences pref = prefs[i];

				if (b_warnLevel.isEnabled()) {
					pref.setWarnLevel(b_warnLevel.getSelectionIndex() + 1);
				}
				if (b_defaultLibrary.isEnabled()) {
					pref.setDefaultLibrary(MesonDefaultLibrary.values()[b_defaultLibrary.getSelectionIndex()]);
				}
				if (b_buildType.isEnabled()) {
					pref.setBuildType(b_buildType.getSelectionIndex() == 0 ? null
							: MesonBuildType.values()[b_buildType.getSelectionIndex() - 1]);
				}
				if (shouldSaveButtonSelection(b_errorLogs))
					pref.setErrorLogs(b_errorLogs.getSelection());
				if (shouldSaveButtonSelection(b_werror))
					pref.setWarningsAsErrors(b_werror.getSelection());
				if (t_outputFolder.getEditable()) {
					final String dir = t_outputFolder.getText();
					pref.setBuildDirectory(dir.trim().isEmpty() ? null : dir);
				}
			}
		} else {
			// we are editing a single configuration...
			MesonPreferences pref = prefs[0];
			pref.setWarnLevel(b_warnLevel.getSelectionIndex() + 1);
			pref.setDefaultLibrary(MesonDefaultLibrary.values()[b_defaultLibrary.getSelectionIndex()]);
			pref.setBuildType(b_buildType.getSelectionIndex() == 0 ? null
					: MesonBuildType.values()[b_buildType.getSelectionIndex() - 1]);
			pref.setStdSplit(b_errorLogs.getSelection());
			pref.setWarningsAsErrors(b_werror.getSelection());
			final String dir = t_outputFolder.getText().trim();
			pref.setBuildDirectory(dir.isEmpty() ? null : dir);
		}
	}

	/**
	 * Switches the specified button behavior from tri-state mode to toggle mode.
	 *
	 * @param button
	 *            the button to modify
	 * @param buttonSelected
	 *            the selection of the button
	 */
	private static void enterToggleMode(Button button, boolean buttonSelected) {
		button.setData(null); // mark toggle mode
		button.setSelection(buttonSelected);
		button.setGrayed(false);
	}

	/**
	 * Switches the specified button behavior from toggle mode to tri-state mode.
	 *
	 * @param button
	 *            the button to modify
	 */
	private static void enterTristateOrToggleMode(Button button, BitSet bs, int numBits) {
		if (needsTri(bs, numBits)) {
			enterTristateMode(button);
		} else {
			enterToggleMode(button, !bs.isEmpty());
		}
	}

	/**
	 * Switches the specified button behavior to toggle mode or to tri-state mode.
	 *
	 * @param button
	 *            the button to modify
	 */
	private static void enterTristateMode(Button button) {
		button.setData(Boolean.TRUE); // mark in tri-state mode
		button.setSelection(true);
		button.setGrayed(true);
	}

	/**
	 * Gets whether all bits in the bit set have the same state.
	 */
	private static boolean needsTri(BitSet bs, int numBits) {
		final int card = bs.cardinality();
		return !(card == numBits || card == 0);
	}

	/**
	 * Invoked when project configuration changes??
	 *
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription,
	 *      org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		// make sure the displayed values get applied
		// AFAICS, src is always == getResDesc(). so saveToModel() effectively
		// stores to src
		saveToModel();

		ICConfigurationDescription srcCfg = src.getConfiguration();
		ICConfigurationDescription dstCfg = dst.getConfiguration();

		if (srcCfg instanceof ICMultiConfigDescription) {
			ICConfigurationDescription[] srcCfgs = (ICConfigurationDescription[]) ((ICMultiConfigDescription) srcCfg)
					.getItems();
			ICConfigurationDescription[] dstCfgs = (ICConfigurationDescription[]) ((ICMultiConfigDescription) dstCfg)
					.getItems();
			for (int i = 0; i < srcCfgs.length; i++) {
				applyConfig(srcCfgs[i], dstCfgs[i]);
			}
		} else {
			applyConfig(srcCfg, dstCfg);
		}
	}

	/**
	 * @param srcCfg
	 * @param dstCfg
	 */
	private static void applyConfig(ICConfigurationDescription srcCfg, ICConfigurationDescription dstCfg) {
		final ConfigurationManager configMgr = ConfigurationManager.getInstance();
		try {
			MesonPreferences srcPrefs = configMgr.getOrLoad(srcCfg);
			MesonPreferences dstPrefs = configMgr.getOrCreate(dstCfg);
			if (srcPrefs != dstPrefs) {
				dstPrefs.setBuildDirectory(srcPrefs.getBuildDirectory());
				dstPrefs.setErrorLogs(srcPrefs.isErrorLogs());
				dstPrefs.setWarningsAsErrors(srcPrefs.isWarningsAsErrors());
				dstPrefs.setWarnLevel(srcPrefs.getWarnLevel());
				dstPrefs.setDefaultLibrary(srcPrefs.getDefaultLibrary());
				dstPrefs.setBuildType(srcPrefs.getBuildType());
			}
		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, CdtPlugin.PLUGIN_ID, null, ex));
		}
	}

	protected void performOK() {
		final ICResourceDescription resDesc = getResDesc();
		if (resDesc == null)
			return;

		saveToModel();
		ICConfigurationDescription cfgd = resDesc.getConfiguration();
		// save project properties..
		try {
			if (prefs.length > 1) {
				// we are editing multiple configurations...
				ICConfigurationDescription[] cfgs = (ICConfigurationDescription[]) ((ICMultiConfigDescription) cfgd)
						.getItems();

				for (int i = 0; i < prefs.length; i++) {
					ICStorageElement storage = cfgs[i].getStorage(MesonPreferences.CFG_STORAGE_ID, true);
					prefs[i].saveToStorage(storage);
				}
			} else {
				// we are editing a single configuration...
				ICStorageElement storage = cfgd.getStorage(MesonPreferences.CFG_STORAGE_ID, true);
				prefs[0].saveToStorage(storage);
			}
		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, CdtPlugin.PLUGIN_ID, null, ex));
		}
	}

	/**
	 * Gets whether the value of the specified button should be saved.
	 *
	 * @param button
	 *            the button to query
	 */
	private static boolean shouldSaveButtonSelection(Button button) {
		if (button != null && Boolean.TRUE.equals(button.getData()) && button.getGrayed()) {
			// if button is in tri-state mode and grayed, do not save
			return false;
		}
		return true;
	}

	/*-
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		for (MesonPreferences pref : prefs) {
			pref.reset();
		}
		updateDisplay();
	}

	// This page can be displayed for project
	@Override
	public boolean canBeVisible() {
		return page.isForProject();
	}

	@Override
	protected void updateButtons() {
		// never called from superclass, but abstract :-)
	}

	////////////////////////////////////////////////////////////////////
	// inner classes
	////////////////////////////////////////////////////////////////////
	/**
	 * Adds tri-state behavior to a button when added as a SWT.Selection listener.
	 *
	 * @author Martin Weber @author Emerald Icemoon
	 */
	private static class TriStateButtonListener implements Listener {

		/*-
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		@Override
		public void handleEvent(Event event) {
			final Button btn = (Button) event.widget;
			if (btn != null && Boolean.TRUE.equals(btn.getData())) {
				// button is in tri-state mode
				if (btn.getSelection()) {
					if (!btn.getGrayed()) {
						btn.setGrayed(true);
					}
				} else {
					if (btn.getGrayed()) {
						btn.setGrayed(false);
						btn.setSelection(true);
					}
				}
			}
		}
	} // TriStateButtonListener
}
