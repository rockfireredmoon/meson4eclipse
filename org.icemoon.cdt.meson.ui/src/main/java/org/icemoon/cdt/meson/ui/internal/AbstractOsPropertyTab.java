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

import java.util.EnumSet;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.MesonBackend;
import org.icemoon.cdt.meson.core.ProjectOption;
import org.icemoon.cdt.meson.core.settings.AbstractOsPreferences;
import org.icemoon.cdt.meson.core.settings.ConfigurationManager;
import org.icemoon.cdt.meson.core.settings.MesonPreferences;

/**
 * Generic UI to control host OS specific project properties and preferences for
 * {@code meson}. Host OS specific properties override generic properties when
 * passed to {@code meson} and get automatically applied if this plugin detects
 * it is running under that operating system.<br>
 * This tab and any subclass is responsible for storing its values.<br>
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 * @param <P>
 *            the type that holds the OS specific properties.
 */
public abstract class AbstractOsPropertyTab<P extends AbstractOsPreferences> extends QuirklessAbstractCPropertyTab {

	/**  */
	private static final ILog log = Activator.getDefault().getLog();

	/** browse files for meson executable */
	private Button b_cmdBrowseFiles;

	/** 'use exec from path' checkbox */
	private Button b_cmdFromPath;
	/** variables in meson executable text field */
	private Button b_cmdVariables;
	/** Combo that shows the generator names for meson */
	private ComboViewer c_generator;
	/**
	 * the preferences associated with our configuration to manage. Initialized in
	 * {@link #configSelectionChanged}
	 */
	private P prefs;
	/** the table showing the meson defines */
	private ProjectOptionsViewer projectOptionsViewer;

	/** meson executable */
	private Text t_cmd;

	/**
	 */
	public AbstractOsPropertyTab() {
	}

	@Override
	public boolean canBeVisible() {
		return page.isForProject();
	}

	@Override
	public boolean canSupportMultiCfg() {
		return false;
	}

	@Override
	protected void configSelectionChanged(ICResourceDescription lastConfig, ICResourceDescription newConfig) {
		if (lastConfig != null) {
			saveToModel();
		}
		if (newConfig == null)
			return;

		if (page.isMultiCfg()) {
			// we are editing multiple configurations...
			setAllVisible(false, null);
			return;
		} else {
			setAllVisible(true, null);

			ICConfigurationDescription cfgd = newConfig.getConfiguration();
			final ConfigurationManager configMgr = ConfigurationManager.getInstance();
			try {
				MesonPreferences allPrefs = configMgr.getOrLoad(cfgd);
				prefs = getOsPreferences(allPrefs);
			} catch (CoreException ex) {
				log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
				MesonPreferences allPrefs = configMgr.getOrCreate(cfgd);
				prefs = getOsPreferences(allPrefs);
			}

			updateDisplay();
		}
	}

	@Override
	protected void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
		// usercomp.setBackground(BACKGROUND_FOR_USER_VAR);

		// meson executable group...
		{
			GridLayout layout;
			Group gr = WidgetHelper.createGroup(usercomp, SWT.FILL, 2, Messages.AbstractOsPropertyTab_0, 2);

			b_cmdFromPath = WidgetHelper.createCheckbox(gr, SWT.BEGINNING, 2,
					Messages.AbstractOsPropertyTab_1);

			setupLabel(gr, Messages.AbstractOsPropertyTab_2, 1, SWT.BEGINNING);

			t_cmd = setupText(gr, 1, GridData.FILL_HORIZONTAL);

			// "Filesystem", "Variables" dialog launcher buttons...
			Composite buttonBar = new Composite(gr, SWT.NONE);
			{
				buttonBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 3, 1));
				layout = new GridLayout(2, false);
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				buttonBar.setLayout(layout);
			}
			b_cmdBrowseFiles = WidgetHelper.createButton(buttonBar, Messages.AbstractOsPropertyTab_3, true);
			b_cmdBrowseFiles.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IDialogSettings settings = Activator.getDefault().getDialogSettings();
					FileDialog dialog = new FileDialog(t_cmd.getShell());
					dialog.setFilterPath(settings.get("meson_dir")); //$NON-NLS-1$
					String text = dialog.open();
					settings.put("meson_dir", dialog.getFilterPath()); //$NON-NLS-1$
					if (text != null) {
						t_cmd.setText(text);
					}
				}
			});

			b_cmdVariables = WidgetHelper.createButton(buttonBar, Messages.AbstractOsPropertyTab_6, true);
			b_cmdVariables.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final ICResourceDescription resDesc = getResDesc();
					if (resDesc == null)
						return;
					ICConfigurationDescription cfgd = resDesc.getConfiguration();
					String text = AbstractCPropertyTab.getVariableDialog(t_cmd.getShell(), cfgd);
					if (text != null) {
						t_cmd.insert(text);
					}
				}
			});
			// to adjust sensitivity...
			b_cmdFromPath.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					final Button btn = (Button) event.widget;
					handleCommandEnabled(!btn.getSelection());
				}
			});

		} // meson executable group

		// makefile generator combo...
		{
			setupLabel(usercomp, Messages.AbstractOsPropertyTab_7, 1, SWT.BEGINNING);
			c_generator = new ComboViewer(usercomp);
			final GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
			gd.widthHint = 200;
			c_generator.getCombo().setLayoutData(gd);
			c_generator.setContentProvider(ArrayContentProvider.getInstance());
			c_generator.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof MesonBackend) {
						return ((MesonBackend) element).getBackendName();
					}
					return super.getText(element);
				}

			});
			final EnumSet<MesonBackend> generators = getAvailableGenerators();
			c_generator.setInput(generators);
			if (generators.size() == 1)
				c_generator.getCombo().setEnabled(false);
		} // makefile generator combo

		// meson defines table...
		final ICResourceDescription resDesc = getResDesc();

		final Group gr = WidgetHelper.createGroup(usercomp, SWT.FILL, 2, Messages.AbstractOsPropertyTab_8, 2);
		projectOptionsViewer = new ProjectOptionsViewer(gr, resDesc == null ? null : resDesc.getConfiguration());
	}

	/**
	 * Gets all sensible choices for meson's generator option on this platform.
	 *
	 * @return a non-empty set, never {@code null}.
	 */
	protected abstract EnumSet<MesonBackend> getAvailableGenerators();

	/**
	 * Gets the OS specific preferences from the specified generic preferences.
	 *
	 * @return the OS specific preferences, never {@code null}.
	 */
	protected abstract P getOsPreferences(MesonPreferences prefs);

	/**
	 * Invoked when project configuration changes?? At least when apply button is
	 * pressed.
	 *
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription,
	 *      org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		// make sure the displayed values get applied
		saveToModel();

		ICConfigurationDescription srcCfg = src.getConfiguration();
		ICConfigurationDescription dstCfg = dst.getConfiguration();
		final ConfigurationManager configMgr = ConfigurationManager.getInstance();

		try {
			P srcPrefs = getOsPreferences(configMgr.getOrLoad(srcCfg));
			P dstPrefs = getOsPreferences(configMgr.getOrCreate(dstCfg));
			if (srcPrefs != dstPrefs) {
				dstPrefs.setUseDefaultCommand(srcPrefs.getUseDefaultCommand());
				dstPrefs.setCommand(srcPrefs.getCommand());
				dstPrefs.setBackend(srcPrefs.getBackend());

				final List<ProjectOption> options = dstPrefs.getProjectOptions();
				options.clear();
				for (ProjectOption def : srcPrefs.getProjectOptions()) {
					options.add(def.clone());
				}

			}
		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
		}
	}

	@Override
	protected void performDefaults() {
		prefs.reset();
		updateDisplay();
	}

	@Override
	protected void performOK() {
		final ICResourceDescription resDesc = getResDesc();
		if (resDesc == null)
			return;

		ICConfigurationDescription cfgd = resDesc.getConfiguration();
		if (cfgd instanceof ICMultiConfigDescription) {
			// this tab does not support editing of multiple configurations
			return;
		}
		saveToModel();
		try {
			// save as project settings..
			ICStorageElement storage = cfgd.getStorage(MesonPreferences.CFG_STORAGE_ID, true);
			prefs.saveToStorage(storage);
		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, ex));
		}
	}

	@Override
	protected void updateButtons() {
		// never called from superclass, but abstract :-)
	}

	/**
	 * Changes sensitivity of controls to enter the meson command. Necessary since
	 * Button.setSelection does not fire events.
	 *
	 * @param enabled
	 *            the new enabled state
	 */
	private void handleCommandEnabled(boolean enabled) {
		t_cmd.setEnabled(enabled);
		b_cmdBrowseFiles.setEnabled(enabled);
		b_cmdVariables.setEnabled(enabled);
	}

	/**
	 * Stores displayed values to the preferences edited by this tab.
	 *
	 * @see #updateDisplay()
	 */
	private void saveToModel() {
		if (prefs == null)
			return;
		prefs.setUseDefaultCommand(b_cmdFromPath.getSelection());
		String command = t_cmd.getText().trim();
		prefs.setCommand(command);

		final IStructuredSelection sel = (IStructuredSelection) c_generator.getSelection();
		prefs.setBackend((MesonBackend) sel.getFirstElement());
		// NB: defines & undefines are modified by the widget listeners directly
	}

	/**
	 * Updates displayed values according to the preferences edited by this tab.
	 */
	private void updateDisplay() {
		t_cmd.setText(prefs.getCommand() == null ? "" : prefs.getCommand()); //$NON-NLS-1$
		b_cmdFromPath.setSelection(prefs.getUseDefaultCommand());
		// adjust sensitivity...
		handleCommandEnabled(!prefs.getUseDefaultCommand());

		MesonBackend generator = prefs.getBackend();
		c_generator.setSelection(new StructuredSelection(generator));

		projectOptionsViewer.setInput(prefs.getProjectOptions());
	}
}
