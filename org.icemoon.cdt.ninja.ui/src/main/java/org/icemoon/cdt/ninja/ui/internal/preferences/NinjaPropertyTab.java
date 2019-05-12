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
 *      Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.preferences;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.icemoon.cdt.ninja.core.NinjaConstants;
import org.osgi.service.prefs.Preferences;

/**
 * UI to control general project properties for meson. This tab is responsible
 * for storing its values.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class NinjaPropertyTab extends QuirklessAbstractCPropertyTab {
	private Label buildCommandLabel;
	private Text buildCommandText;
	private Label cleanCommandLabel;
	private Text cleanCommandText;
	private ICResourceDescription selectedConfig;
	private Button useCustomCommands;

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
		if (newConfig == null)
			return;
		selectedConfig = newConfig;
		updateDisplay();
	}

	@Override
	protected void createControls(final Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
		useCustomCommands = ControlFactory.createCheckBox(usercomp, (Messages.NinjaPropertyPage_UseCustom));
		((GridData) useCustomCommands.getLayoutData()).horizontalSpan = 2;
		useCustomCommands.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablements();
			}
		});

		buildCommandLabel = new Label(usercomp, SWT.NONE);
		buildCommandLabel.setText(Messages.NinjaPropertyPage_BuildCommand);

		buildCommandText = new Text(usercomp, SWT.BORDER);
		buildCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		cleanCommandLabel = new Label(usercomp, SWT.NONE);
		cleanCommandLabel.setText(Messages.NinjaPropertyPage_CleanCommand);

		cleanCommandText = new Text(usercomp, SWT.BORDER);
		cleanCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		updateEnablements();

	}
	
	protected String getConfigName(ICConfigurationDescription cfgd) {
		// TODO?
		return IBuildConfiguration.DEFAULT_CONFIG_NAME;
	}

	protected Preferences getSettings(ICConfigurationDescription cfgd) {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config") //$NON-NLS-1$
				.node(page.getProject().getName()).node(getConfigName(cfgd));
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
	}

	@Override
	protected void performDefaults() {
		updateDisplay();
	}

	@Override
	protected void performOK() {
		final ICResourceDescription resDesc = getResDesc();
		if (resDesc == null)
			return;
		ICConfigurationDescription cfgd = resDesc.getConfiguration();
		if (cfgd instanceof ICMultiConfigDescription) {
			ICConfigurationDescription[] cfgs = (ICConfigurationDescription[]) ((ICMultiConfigDescription) cfgd)
					.getItems();
			for (int i = 0; i < cfgs.length; i++) {
				saveToStorage(cfgs[i]);
			}
		} else {
			saveToStorage(cfgd);
		}
	}

	@Override
	protected void updateButtons() {
		updateEnablements();
	}

	private void saveToStorage(ICConfigurationDescription cfgd) {
		Preferences settings = getSettings(cfgd);
		settings.putBoolean(NinjaConstants.USE_CUSTOM_COMMANDS, useCustomCommands.getSelection());
		settings.put(NinjaConstants.BUILD_COMMAND, buildCommandText.getText());
		settings.put(NinjaConstants.CLEAN_COMMAND, cleanCommandText.getText());
	}

	private void updateDisplay() {
		final ICResourceDescription resDesc = selectedConfig == null ? getResDesc() : selectedConfig;
		if (resDesc == null)
			return;

		ICConfigurationDescription cfgd = resDesc.getConfiguration();
		Preferences settings = getSettings(cfgd);
		useCustomCommands.setSelection(settings.getBoolean(NinjaConstants.USE_CUSTOM_COMMANDS, false));
		buildCommandText.setText(settings.get(NinjaConstants.BUILD_COMMAND, "ninja all"));
		cleanCommandText.setText(settings.get(NinjaConstants.CLEAN_COMMAND, "ninja clean"));
		updateEnablements();
	}

	private void updateEnablements() {
		boolean isLoggingEnabled = useCustomCommands.getSelection();
		buildCommandText.setEnabled(isLoggingEnabled);
		cleanCommandText.setEnabled(isLoggingEnabled);
		buildCommandLabel.setEnabled(isLoggingEnabled);
		cleanCommandLabel.setEnabled(isLoggingEnabled);
	}
}
