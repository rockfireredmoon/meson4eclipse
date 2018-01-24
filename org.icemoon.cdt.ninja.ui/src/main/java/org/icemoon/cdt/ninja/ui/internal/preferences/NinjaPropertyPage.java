package org.icemoon.cdt.ninja.ui.internal.preferences;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.icemoon.cdt.ninja.core.corebuild.internal.NinjaBuildConfiguration;

public class NinjaPropertyPage extends PropertyPage {

	private Text buildCommandText;
	private Text cleanCommandText;
	private Button useCustomCommands;

	private Label cleanCommandLabel;

	private Label buildCommandLabel;

	@Override
	protected Control createContents(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		IPreferenceStore prefs = getPreferenceStore();

		useCustomCommands = ControlFactory.createCheckBox(comp, (Messages.NinjaBuildTab_UseCustom));
		((GridData) useCustomCommands.getLayoutData()).horizontalSpan = 2;
		boolean keepLog = prefs.getBoolean(NinjaBuildConfiguration.USE_CUSTOM_COMMANDS);
		useCustomCommands.setSelection(keepLog);
		useCustomCommands.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablements();
			}
		});

		buildCommandLabel = new Label(comp, SWT.NONE);
		buildCommandLabel.setText(Messages.NinjaBuildTab_BuildCommand);

		buildCommandText = new Text(comp, SWT.BORDER);
		buildCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		cleanCommandLabel = new Label(comp, SWT.NONE);
		cleanCommandLabel.setText(Messages.NinjaBuildTab_CleanCommand);

		cleanCommandText = new Text(comp, SWT.BORDER);
		cleanCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		updateEnablements();
		return comp;
	}

	@Override
	public boolean performOk() {
		IPreferenceStore prefs = getPreferenceStore();
		prefs.setValue(NinjaBuildConfiguration.USE_CUSTOM_COMMANDS, useCustomCommands.getSelection());
		prefs.setValue(NinjaBuildConfiguration.BUILD_COMMAND, buildCommandText.getText());
		prefs.setValue(NinjaBuildConfiguration.CLEAN_COMMAND, cleanCommandText.getText());
		return true;
	}

	private void updateEnablements() {
		boolean isLoggingEnabled = useCustomCommands.getSelection();
		buildCommandText.setEnabled(isLoggingEnabled);
		cleanCommandText.setEnabled(isLoggingEnabled);
		buildCommandLabel.setEnabled(isLoggingEnabled);
		cleanCommandLabel.setEnabled(isLoggingEnabled);
	}
}
