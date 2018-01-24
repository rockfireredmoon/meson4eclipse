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

import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.launch.ui.corebuild.CommonBuildTab;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.icemoon.cdt.ninja.core.corebuild.internal.NinjaBuildConfiguration;
import org.icemoon.cdt.ninja.core.corebuild.internal.NinjaBuildConfigurationProvider;

public class NinjaBuildTab extends CommonBuildTab {

	private Text buildCommandText;
	private Text cleanCommandText;
	private Button useCustomCommands;
	private Label cleanCommandLabel;
	private Label buildCommandLabel;

	@Override
	protected String getBuildConfigProviderId() {
		return NinjaBuildConfigurationProvider.ID;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());

		useCustomCommands = ControlFactory.createCheckBox(comp, (Messages.NinjaBuildTab_UseCustom));
		useCustomCommands.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablements();
			}
		});
		((GridData) useCustomCommands.getLayoutData()).horizontalSpan = 2;

		buildCommandLabel = new Label(comp, SWT.NONE);
		buildCommandLabel.setText(Messages.NinjaBuildTab_BuildCommand);

		buildCommandText = new Text(comp, SWT.BORDER);
		buildCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		cleanCommandLabel = new Label(comp, SWT.NONE);
		cleanCommandLabel.setText(Messages.NinjaBuildTab_CleanCommand);

		cleanCommandText = new Text(comp, SWT.BORDER);
		cleanCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		ICBuildConfiguration buildConfig = getBuildConfiguration();

		String buildCommand = buildConfig.getProperty(NinjaBuildConfiguration.BUILD_COMMAND);
		if (buildCommand != null) {
			buildCommandText.setText(buildCommand);
		} else {
			buildCommandText.setText(""); //$NON-NLS-1$
		}

		String cleanCommand = buildConfig.getProperty(NinjaBuildConfiguration.CLEAN_COMMAND);
		if (cleanCommand != null) {
			cleanCommandText.setText(buildCommand);
		} else {
			cleanCommandText.setText(""); //$NON-NLS-1$
		}
		useCustomCommands
				.setSelection("true".equals(buildConfig.getProperty(NinjaBuildConfiguration.USE_CUSTOM_COMMANDS)));
		updateEnablements();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		ICBuildConfiguration buildConfig = getBuildConfiguration();
		if (useCustomCommands.getSelection()) {
			String buildCommand = buildCommandText.getText().trim();
			if (!buildCommand.isEmpty()) {
				buildConfig.setProperty(NinjaBuildConfiguration.BUILD_COMMAND, buildCommand);
			} else {
				buildConfig.removeProperty(NinjaBuildConfiguration.BUILD_COMMAND);
			}

			String cleanCommand = cleanCommandText.getText().trim();
			if (!cleanCommand.isEmpty()) {
				buildConfig.setProperty(NinjaBuildConfiguration.CLEAN_COMMAND, cleanCommand);
			} else {
				buildConfig.removeProperty(NinjaBuildConfiguration.CLEAN_COMMAND);
			}
		} else {
			buildConfig.removeProperty(NinjaBuildConfiguration.BUILD_COMMAND);
			buildConfig.removeProperty(NinjaBuildConfiguration.CLEAN_COMMAND);
		}
	}

	@Override
	protected void saveProperties(Map<String, String> properties) {
		super.saveProperties(properties);
		properties.put(NinjaBuildConfiguration.BUILD_COMMAND, buildCommandText.getText().trim());
		properties.put(NinjaBuildConfiguration.CLEAN_COMMAND, cleanCommandText.getText().trim());
	}

	@Override
	protected void restoreProperties(Map<String, String> properties) {
		super.restoreProperties(properties);

		String buildCmd = properties.get(NinjaBuildConfiguration.BUILD_COMMAND);
		if (buildCmd != null) {
			buildCommandText.setText(buildCmd);
		} else {
			buildCommandText.setText(""); //$NON-NLS-1$
		}

		String cleanCmd = properties.get(NinjaBuildConfiguration.CLEAN_COMMAND);
		if (cleanCmd != null) {
			cleanCommandText.setText(cleanCmd);
		} else {
			cleanCommandText.setText(""); //$NON-NLS-1$
		}
	}

	@Override
	public String getName() {
		return Messages.NinjaBuildTab_Ninja;
	}

	private void updateEnablements() {
		boolean isLoggingEnabled = useCustomCommands.getSelection();
		buildCommandText.setEnabled(isLoggingEnabled);
		cleanCommandText.setEnabled(isLoggingEnabled);
		buildCommandLabel.setEnabled(isLoggingEnabled);
		cleanCommandLabel.setEnabled(isLoggingEnabled);
	}
}
