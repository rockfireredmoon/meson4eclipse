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
import java.util.Objects;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.icemoon.cdt.meson.core.CdtPlugin;
import org.icemoon.cdt.meson.core.internal.settings.ConfigurationManager;
import org.icemoon.cdt.meson.core.internal.settings.MesonLayout;
import org.icemoon.cdt.meson.core.internal.settings.MesonPreferences;
import org.icemoon.cdt.meson.core.internal.settings.MesonUnity;

/**
 * UI to control general project properties for meson. This tab is responsible
 * for storing its values.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonAdvancedTab extends QuirklessAbstractCPropertyTab {

	/**  */
	private static final ILog log = CdtPlugin.getDefault().getLog();

	// Widgets
	private Combo b_layout;
	private Combo b_unity;
	private Button b_stripTargets;
	private Button b_stdSplit;
	/** pre-populate cache from file */
	private Text t_crossFile;
	/** browse files for cache file */
	private Button b_browseCrossFile;
	/** build directory */

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

		// Meson options group...
		{

			setupLabel(usercomp, "Build Directory Layout \t(--layout)", 1, SWT.BEGINNING);
			b_layout = WidgetHelper.createCombo(usercomp, SWT.BEGINNING, 2, MesonLayout.MIRROR.name(), true,
					MesonLayout.names());
			b_layout.addListener(SWT.Selection, tsl);
			setupLabel(usercomp, "Unity \t(--unity)", 1, SWT.BEGINNING);
			b_unity = WidgetHelper.createCombo(usercomp, SWT.BEGINNING, 2, MesonUnity.OFF.name(), true,
					MesonUnity.names());
			b_unity.addListener(SWT.Selection, tsl);
			b_stripTargets = WidgetHelper.createCheckbox(usercomp, SWT.BEGINNING, 2,
					"Strip targets on install (--strip)");
			b_stripTargets.addListener(SWT.Selection, tsl);
			b_stdSplit = WidgetHelper.createCheckbox(usercomp, SWT.BEGINNING, 2,
					"Split stdout and stderr in test logs \t(--stdsplit)");
			b_stdSplit.addListener(SWT.Selection, tsl);

			// Cross compilation group...
			{
				Group gr2 = WidgetHelper.createGroup(usercomp, SWT.FILL, 2,
						"Cross compilation environment (--cross-file)", 2);

				setupLabel(gr2, "&File", 1, SWT.BEGINNING);

				t_crossFile = setupText(gr2, 1, GridData.FILL_HORIZONTAL);
				// "Browse..." dialog launcher buttons...
				b_browseCrossFile = WidgetHelper.createButton(gr2, "&Browse...", true);
				b_browseCrossFile.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 2, 1));

				b_browseCrossFile.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(
								t_crossFile.getShell(), false, page.getProject(), IResource.FILE);
						dialog.setTitle("Select file");
						dialog.setInitialPattern("*.txt", FilteredItemsSelectionDialog.FULL_SELECTION);
						dialog.open();
						IFile file = (IFile) dialog.getFirstResult();
						if (file != null) {
							// insert selected resource name
							t_crossFile.insert(file.getProjectRelativePath().toPortableString());
						}
					}
				});

			} // "Cross compilation group
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
	 * Sets the value of the cache file entry field and whether the user can edit
	 * that input field.
	 *
	 * @param text
	 *            the text to display in the cache-file field
	 */
	private void setCacheFileEditable(boolean editable, String text) {
		text = editable ? text : " <configurations differ> ";
		t_crossFile.setText(text == null ? "" : text);
		t_crossFile.setEditable(editable);
		t_crossFile.setEnabled(editable);
		b_browseCrossFile.setEnabled(editable);
	}

	/**
	 * Updates displayed values according to the preferences edited by this tab.
	 */
	private void updateDisplay() {
		boolean cacheFileEditable = true;
		boolean layoutEditable = true;
		boolean unityEditable = true;

		if (prefs.length > 1) {
			// we are editing multiple configurations...
			/*
			 * make each button tri-state, if its settings are not the same in all
			 * configurations
			 */
			BitSet bs = new BitSet(prefs.length);

			// b_stripTargets
			bs.clear();
			for (int i = 0; i < prefs.length; i++) {
				bs.set(i, prefs[i].isStrip());
			}
			enterTristateOrToggleMode(b_stripTargets, bs, prefs.length);

			// b_stdSplit...
			bs.clear();
			for (int i = 0; i < prefs.length; i++) {
				bs.set(i, prefs[i].isStdSplit());
			}
			enterTristateOrToggleMode(b_stdSplit, bs, prefs.length);
			// b_layout
			/*
			 * make b_layout disabled, if its settings are not the same in all
			 * configurations
			 */
			{
				final MesonLayout ml0 = prefs[0].getLayout();
				for (int i = 1; i < prefs.length; i++) {
					MesonLayout ml = prefs[i].getLayout();
					if (Objects.equals(ml0, ml)) {
						layoutEditable = false;
						break;
					}
				}
			}
			// b_unity
			/*
			 * make b_unity disabled, if its settings are not the same in all configurations
			 */
			{
				final MesonUnity ml0 = prefs[0].getUnity();
				for (int i = 1; i < prefs.length; i++) {
					MesonUnity ml = prefs[i].getUnity();
					if (Objects.equals(ml0, ml)) {
						unityEditable = false;
						break;
					}
				}
			}

			// t_crossFile
			/*
			 * make t_crossFile disabled, if its settings are not the same in all
			 * configurations
			 */
			{
				final String cf0 = prefs[0].getCrossFile();
				for (int i = 1; i < prefs.length; i++) {
					String cf = prefs[i].getCrossFile();
					if (cf0 != null) {
						if (!cf0.equals(cf)) {
							// configurations differ
							cacheFileEditable = false;
							break;
						}
					} else if (cf != null) {
						// configurations differ
						cacheFileEditable = false;
						break;
					}
				}
			}
		} else {
			// we are editing a single configuration...
			// all buttons are in toggle mode
			MesonPreferences pref = prefs[0];
			int idx = Arrays.asList(b_layout.getItems()).indexOf(String.valueOf(pref.getLayout().name()));
			if (idx != -1)
				b_layout.select(idx);
			idx = Arrays.asList(b_unity.getItems()).indexOf(String.valueOf(pref.getUnity().name()));
			if (idx != -1)
				b_unity.select(idx);
			enterToggleMode(b_stdSplit, pref.isStdSplit());
		}

		b_layout.setEnabled(layoutEditable);
		b_unity.setEnabled(unityEditable);
		setCacheFileEditable(cacheFileEditable, prefs[0].getCrossFile());
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
				if (b_layout.isEnabled())
					pref.setLayout(MesonLayout.values()[b_layout.getSelectionIndex()]);
				if (b_unity.isEnabled())
					pref.setUnity(MesonUnity.values()[b_unity.getSelectionIndex()]);
				if (shouldSaveButtonSelection(b_stripTargets))
					pref.setStrip(b_stripTargets.getSelection());
				if (shouldSaveButtonSelection(b_stdSplit))
					pref.setStdSplit(b_stdSplit.getSelection());
				if (t_crossFile.getEditable()) {
					final String cacheFileName = t_crossFile.getText();
					pref.setCrossFile(cacheFileName.trim().isEmpty() ? null : cacheFileName);
				}
			}
		} else {
			// we are editing a single configuration...
			MesonPreferences pref = prefs[0];
			pref.setLayout(MesonLayout.values()[b_layout.getSelectionIndex()]);
			pref.setUnity(MesonUnity.values()[b_unity.getSelectionIndex()]);
			pref.setStrip(b_stripTargets.getSelection());
			pref.setStdSplit(b_stdSplit.getSelection());
			final String cacheFileName = t_crossFile.getText().trim();
			pref.setCrossFile(cacheFileName.isEmpty() ? null : cacheFileName);
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
				dstPrefs.setStrip(srcPrefs.isStrip());
				dstPrefs.setStdSplit(srcPrefs.isStdSplit());
				dstPrefs.setUnity(srcPrefs.getUnity());
				dstPrefs.setCrossFile(srcPrefs.getCrossFile());
				dstPrefs.setLayout(srcPrefs.getLayout());
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
