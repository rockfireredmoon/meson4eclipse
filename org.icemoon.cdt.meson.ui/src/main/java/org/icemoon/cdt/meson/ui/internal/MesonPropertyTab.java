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

import java.util.BitSet;
import java.util.Objects;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.NewFolderDialog;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.settings.ConfigurationManager;
import org.icemoon.cdt.meson.core.settings.MesonBuildType;
import org.icemoon.cdt.meson.core.settings.MesonDefaultLibrary;
import org.icemoon.cdt.meson.core.settings.MesonPreferences;

/**
 * UI to control general project properties for meson. This tab is responsible
 * for storing its values.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonPropertyTab extends QuirklessAbstractCPropertyTab {

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

	/**  */
	private static final ILog log = Activator.getDefault().getLog();

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
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
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
	 * Gets whether all bits in the bit set have the same state.
	 */
	private static boolean needsTri(BitSet bs, int numBits) {
		final int card = bs.cardinality();
		return !(card == numBits || card == 0);
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

	private Button b_browseOutputFolder;
	private ComboViewer b_buildType;

	/** variables in output folder text field */
	private Button b_cmdVariables;

	private Button b_createOutputFolder;

	private ComboViewer b_defaultLibrary;

	private Button b_errorLogs;

	// Widgets
	private ComboViewer b_warnLevel;

	private Button b_werror;

	/**
	 * the preferences associated with our configurations to manage. Initialized in
	 * {@link #updateData}
	 */
	private MesonPreferences[] prefs;

	/** build directory */
	private Text t_outputFolder;

	/** shared listener for check-boxes */
	private TriStateButtonListener tsl = new TriStateButtonListener();

	// This page can be displayed for project
	@Override
	public boolean canBeVisible() {
		return page.isForProject();
	}

	public boolean isManaged() {
		try {
			return page.getProject().getNature(ManagedCProjectNature.MNG_NATURE_ID) != null;
		} catch (CoreException e) {
			return false;
		}
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
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
		}
		updateDisplay();
	}

	@Override
	protected void createControls(final Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
		// usercomp.setBackground(BACKGROUND_FOR_USER_VAR);

		// output folder group
		{
			Group gr = WidgetHelper.createGroup(usercomp, SWT.FILL, 2,
					Messages.MesonPropertyTab_0, 2);

			setupLabel(gr, Messages.MesonPropertyTab_1, 1, SWT.BEGINNING);

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
			b_browseOutputFolder = WidgetHelper.createButton(buttonBar, Messages.MesonPropertyTab_2, true);
			b_browseOutputFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(
							b_browseOutputFolder.getShell(), false, page.getProject(), IResource.FOLDER);
					dialog.setTitle(Messages.MesonPropertyTab_3);
					dialog.open();
					IFolder folder = (IFolder) dialog.getFirstResult();
					if (folder != null) {
						// insert selected resource name
						t_outputFolder.setText(folder.getProjectRelativePath().toPortableString());
					}
				}
			});
			b_createOutputFolder = WidgetHelper.createButton(buttonBar, Messages.MesonPropertyTab_4, true);
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

			b_cmdVariables = WidgetHelper.createButton(buttonBar, Messages.MesonPropertyTab_5, true);
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
			Group gr = WidgetHelper.createGroup(usercomp, SWT.FILL, 2, Messages.MesonPropertyTab_6, 2);

			setupLabel(gr, Messages.MesonPropertyTab_7, 1, SWT.BEGINNING);
			b_warnLevel = WidgetHelper.createCombo(gr, SWT.BEGINNING, 2, 1, true, 1, 2, 3);

			setupLabel(gr, Messages.MesonPropertyTab_8, 1, SWT.BEGINNING);
			b_buildType = WidgetHelper.createCombo(gr, SWT.BEGINNING, 2, MesonBuildType.DEBUG, true,
					MesonBuildType.values());

			b_buildType.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((MesonBuildType) element).getName();
				}

			});

			setupLabel(gr, Messages.MesonPropertyTab_9, 1, SWT.BEGINNING);
			b_defaultLibrary = WidgetHelper.createCombo(gr, SWT.BEGINNING, 2, MesonDefaultLibrary.SHARED, true,
					MesonDefaultLibrary.values());
			b_defaultLibrary.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((MesonDefaultLibrary) element).getName();
				}

			});

			b_errorLogs = WidgetHelper.createCheckbox(gr, SWT.BEGINNING, 2,
					Messages.MesonPropertyTab_10);
			b_errorLogs.addListener(SWT.Selection, tsl);

			b_werror = WidgetHelper.createCheckbox(gr, SWT.BEGINNING, 2, Messages.MesonPropertyTab_11);
			b_werror.addListener(SWT.Selection, tsl);

		} // Meson options group

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

	@Override
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
					ConfigurationManager.getInstance().save(cfgs[i], prefs[i]);
				}
			} else {
				// we are editing a single configuration...
				ConfigurationManager.getInstance().save(cfgd, prefs[0]);
			}
		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
		}
	}

	@Override
	protected void updateButtons() {
		// never called from superclass, but abstract :-)
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

				if (b_warnLevel.getCombo().isEnabled()) {
					pref.setWarnLevel((Integer) (b_warnLevel.getStructuredSelection().getFirstElement()));
				}
				if (b_defaultLibrary.getCombo().isEnabled()) {
					pref.setDefaultLibrary(
							(MesonDefaultLibrary) b_defaultLibrary.getStructuredSelection().getFirstElement());
				}
				if (b_buildType.getCombo().isEnabled()) {
					pref.setBuildType((MesonBuildType) b_defaultLibrary.getStructuredSelection().getFirstElement());
				}
				if (shouldSaveButtonSelection(b_errorLogs))
					pref.setErrorLogs(b_errorLogs.getSelection());
				if (shouldSaveButtonSelection(b_werror))
					pref.setWarningsAsErrors(b_werror.getSelection());
				if (t_outputFolder.getEditable()) {
					final String dir = t_outputFolder.getText();
					pref.setBuildDirectory(dir.trim().isEmpty() ? null : dir);
				}
				pref.needsReconfigure();
			}
		} else {
			// we are editing a single configuration...
			MesonPreferences pref = prefs[0];
			IStructuredSelection sel = b_warnLevel.getStructuredSelection();
			if(sel == null)
				pref.setWarnLevel(Integer.parseInt(b_warnLevel.getCombo().getText()));
			else
				pref.setWarnLevel((Integer) (sel.getFirstElement()));
			pref.setDefaultLibrary((MesonDefaultLibrary) b_defaultLibrary.getStructuredSelection().getFirstElement());
			pref.setBuildType((MesonBuildType) b_buildType.getStructuredSelection().getFirstElement());
			pref.setStdSplit(b_errorLogs.getSelection());
			pref.setWarningsAsErrors(b_werror.getSelection());
			final String dir = t_outputFolder.getText().trim();
			pref.setBuildDirectory(dir.isEmpty() ? null : dir);
			pref.needsReconfigure();
		}
	}

	/**
	 * Sets the value of the build folder entry field and whether the user can edit
	 * that input field.
	 *
	 * @param text
	 *            the text to display in the cache-file field
	 */
	private void setBuildFolderEditable(boolean editable, String text) {
		text = editable ? text : Messages.MesonPropertyTab_12;
		t_outputFolder.setText(text == null ? "" : text); //$NON-NLS-1$
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
			int warnLevel = pref.getWarnLevel();
			b_warnLevel.setSelection(new StructuredSelection(warnLevel), true);
			b_defaultLibrary.setSelection(
					new StructuredSelection(
							pref.getDefaultLibrary() == null ? MesonDefaultLibrary.DEFAULT : pref.getDefaultLibrary()),
					true);
			b_buildType.setSelection(new StructuredSelection(
					pref.getBuildType() == null ? MesonDefaultLibrary.DEFAULT : pref.getBuildType()), true);

			enterToggleMode(b_errorLogs, pref.isStdSplit());
			enterToggleMode(b_werror, pref.isWarningsAsErrors());
		}

		b_warnLevel.getCombo().setEnabled(warnLevelEditable);
		b_defaultLibrary.getCombo().setEnabled(defaultLibraryEditable);
		b_buildType.getCombo().setEnabled(buildTypeEditable);
		String text = prefs[0].getBuildDirectory();
		setBuildFolderEditable(buildFolderEditable,
				text == null ? (isManaged() ? "builddir/${ConfigName}" : "builddir") : text); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
