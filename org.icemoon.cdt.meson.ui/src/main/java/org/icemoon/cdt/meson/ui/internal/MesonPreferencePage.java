/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *      Emerald Icemoon - Ported to Meson
 *******************************************************************************/
package org.icemoon.cdt.meson.ui.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.IMesonCrossCompileFile;
import org.icemoon.cdt.meson.core.IMesonCrossCompileManager;

/**
 * @author Emerald Icemoon
 */
public class MesonPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Table filesTable;
	private Map<Path, IMesonCrossCompileFile> filesToAdd = new HashMap<>();
	private Map<Path, IMesonCrossCompileFile> filesToRemove = new HashMap<>();

	private IMesonCrossCompileManager manager;
	private Button removeButton;

	@Override
	public void init(IWorkbench workbench) {
		manager = Activator.getService(IMesonCrossCompileManager.class);
	}

	@Override
	public boolean performOk() {
		for (IMesonCrossCompileFile file : filesToAdd.values()) {
			manager.addToolChainFile(file);
		}

		for (IMesonCrossCompileFile file : filesToRemove.values()) {
			manager.removeToolChainFile(file);
		}

		filesToAdd.clear();
		filesToRemove.clear();

		return true;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Group filesGroup = new Group(control, SWT.NONE);
		filesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesGroup.setText(Messages.MesonPreferencePage_Files);
		filesGroup.setLayout(new GridLayout(2, false));

		Composite filesComp = new Composite(filesGroup, SWT.NONE);
		filesComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		filesTable = new Table(filesComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		filesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesTable.setHeaderVisible(true);
		filesTable.setLinesVisible(true);
		filesTable.addListener(SWT.Selection, e -> {
			TableItem[] items = filesTable.getSelection();
			removeButton.setEnabled(items.length > 0);
		});

		TableColumn pathColumn = new TableColumn(filesTable, SWT.NONE);
		pathColumn.setText(Messages.MesonPreferencePage_Path);

		TableColumn tcColumn = new TableColumn(filesTable, SWT.NONE);
		tcColumn.setText(Messages.MesonPreferencePage_Toolchain);

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(pathColumn, new ColumnWeightData(50, 350, true));
		tableLayout.setColumnData(tcColumn, new ColumnWeightData(50, 350, true));
		filesComp.setLayout(tableLayout);

		Composite buttonsComp = new Composite(filesGroup, SWT.NONE);
		buttonsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttonsComp.setLayout(new GridLayout());

		Button addButton = new Button(buttonsComp, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addButton.setText(Messages.MesonPreferencePage_Add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewMesonToolChainFileWizard wizard = new NewMesonToolChainFileWizard();
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					IMesonCrossCompileFile file = wizard.getNewFile();
					if (filesToRemove.containsKey(file.getPath())) {
						filesToRemove.remove(file.getPath());
					} else {
						filesToAdd.put(file.getPath(), file);
					}
					updateTable();
				}
			}
		});

		removeButton = new Button(buttonsComp, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		removeButton.setText(Messages.MesonPreferencePage_Remove);
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection, e -> {
			if (MessageDialog.openConfirm(getShell(), Messages.MesonPreferencePage_ConfirmRemoveTitle,
					Messages.MesonPreferencePage_ConfirmRemoveDesc)) {
				for (TableItem item : filesTable.getSelection()) {
					IMesonCrossCompileFile file = (IMesonCrossCompileFile) item.getData();
					if (filesToAdd.containsKey(file.getPath())) {
						filesToAdd.remove(file.getPath());
					} else {
						filesToRemove.put(file.getPath(), file);
					}
					updateTable();
				}
			}
		});

		updateTable();

		return control;
	}

	private Map<Path, IMesonCrossCompileFile> getFiles() {
		Map<Path, IMesonCrossCompileFile> files = new HashMap<>();
		for (IMesonCrossCompileFile file : manager.getToolChainFiles()) {
			files.put(file.getPath(), file);
		}

		for (IMesonCrossCompileFile file : filesToAdd.values()) {
			files.put(file.getPath(), file);
		}

		for (IMesonCrossCompileFile file : filesToRemove.values()) {
			files.remove(file.getPath());
		}

		return files;
	}

	private void updateTable() {
		List<IMesonCrossCompileFile> sorted = new ArrayList<>(getFiles().values());
		Collections.sort(sorted, (o1, o2) -> o1.getPath().toString().compareToIgnoreCase(o2.getPath().toString()));

		filesTable.removeAll();
		for (IMesonCrossCompileFile file : sorted) {
			TableItem item = new TableItem(filesTable, SWT.NONE);
			item.setText(0, file.getPath().toString());

			try {
				IToolChain tc = file.getToolChain();
				if (tc != null) {
					item.setText(1, tc.getName());
				}
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}

			item.setData(file);
		}
	}

}
