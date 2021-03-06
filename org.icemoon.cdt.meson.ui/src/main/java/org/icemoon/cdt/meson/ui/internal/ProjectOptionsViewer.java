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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.icemoon.cdt.meson.core.ProjectOption;

/**
 * Displays a table for the Meson project options. The created table will be
 * displayed in a group, together with buttons to add, edit and delete table
 * entries.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
/* package */class ProjectOptionsViewer {
	private static class MesonProjectOptionLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

		// // LabelProvider
		// @Override
		// public String getText(Object element) {
		// return getColumnText(element, 0);
		// }

		// interface ITableLabelProvider
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		// interface ITableLabelProvider
		@Override
		public String getColumnText(Object element, int columnIndex) {
			final ProjectOption var = (ProjectOption) element;
			switch (columnIndex) {
			case 0:
				return var.getName();
			case 1:
				return var.getType().name();
			case 2:
				return var.getValue();
			}
			return ""; //$NON-NLS-1$
		}
	} // MesonProjectOptionLabelProvider

	/**
	 * Converts the list of ProjectOption object.
	 *
	 * @author Martin Weber
	 * @author Emerald Icemoon
	 */
	private static class MesonProjectOptionTableContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			@SuppressWarnings("unchecked")
			final List<ProjectOption> elems = (List<ProjectOption>) inputElement;
			return elems.toArray();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	} // MesonProjectOptionTableContentProvider

	private static class MyViewerComparator extends ViewerComparator {
		private boolean ascending;
		private int sortColumn;

		public MyViewerComparator() {
			this.sortColumn = 0;
			ascending = true;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			ProjectOption v1 = (ProjectOption) e1;
			ProjectOption v2 = (ProjectOption) e2;
			int rc = 0;
			switch (sortColumn) {
			case 0:
				rc = v1.getName().compareTo(v2.getName());
				break;
			case 1:
				rc = v1.getType().name().compareTo(v2.getType().name());
				break;
			case 2:
				rc = v1.getValue().compareTo(v2.getValue());
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (!ascending) {
				rc = -rc;
			}
			return rc;
		}

		public int getSortDirection() {
			return ascending ? SWT.UP : SWT.DOWN;
		}

		public void setSortColumn(int column) {
			if (column == this.sortColumn) {
				// Same column as last sort; toggle the direction
				ascending ^= true;
			} else {
				// New column; do an ascending sort
				this.sortColumn = column;
				ascending = true;
			}
		}
	} // MyViewerComparator

	/** table column names */
	private static final String[] tableColumnNames = { Messages.ProjectOptionsViewer_1, Messages.ProjectOptionsViewer_2, Messages.ProjectOptionsViewer_3 };
	/** table column widths */
	private static final int[] tableColumnWidths = { 120, 80, 250 };

	/**
	 * the configuration description to use for variable selection dialog or
	 * <code>null</code>
	 */
	private final ICConfigurationDescription cfgd;

	private MyViewerComparator comparator;

	private TableViewer tableViewer;

	/**
	 * @param parent
	 * @param cfgd
	 *            the configuration description to use for variable selection dialog
	 *            or <code>null</code>
	 */
	public ProjectOptionsViewer(Composite parent, ICConfigurationDescription cfgd) {
		this.cfgd = cfgd;
		createEditor(parent);
		// Set the sorter for the table
		comparator = new MyViewerComparator();
		tableViewer.setComparator(comparator);
	}

	/**
	 * Gets the TableViewer for the Meson project options
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * Sets the list of meson defines that are displayed by the viewer.
	 */
	public void setInput(List<ProjectOption> defines) {
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		tableViewer.setInput(defines);
	}

	/**
	 * Creates the columns for the table.
	 *
	 * @param parent
	 * @param viewer
	 */
	private void createColumns(final Composite parent, final TableViewer viewer) {
		for (int i = 0; i < tableColumnNames.length; i++) {
			createTableViewerColumn(viewer, tableColumnNames[i], tableColumnWidths[i], i);
		}
	}

	/**
	 * Creates the control to add/delete/edit meson-variables to define.
	 */
	private void createEditor(Composite parent) {
		Composite gr = new Composite(parent, SWT.NONE);
		gr.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gr.setLayoutData(gd);
		tableViewer = createViewer(gr);

		// let double click trigger the edit dialog
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				handleDefineEditButton(tableViewer);
			}
		});
		// let DEL key trigger the delete dialog
		tableViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					handleDefineDelButton(tableViewer);
				} else if (e.keyCode == SWT.INSERT) {
					handleDefineAddButton(tableViewer);
				}
			}
		});

		// Buttons, vertically stacked
		Composite editButtons = new Composite(gr, SWT.NONE);
		editButtons.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false));
		editButtons.setLayout(new GridLayout(1, false));
		// editButtons.setBackground(BACKGROUND_FOR_USER_VAR);

		Button buttonDefineAdd = WidgetHelper.createButton(editButtons, AbstractCPropertyTab.ADD_STR, true);
		final Button buttonDefineEdit = WidgetHelper.createButton(editButtons, AbstractCPropertyTab.EDIT_STR, false);
		final Button buttonDefineDel = WidgetHelper.createButton(editButtons, AbstractCPropertyTab.DEL_STR, false);

		// wire button actions...
		buttonDefineAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleDefineAddButton(tableViewer);
			}
		});
		buttonDefineEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleDefineEditButton(tableViewer);
			}
		});
		buttonDefineDel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleDefineDelButton(tableViewer);
			}
		});

		// enable button sensitivity based on table selection
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// update button sensitivity..
				int sels = ((IStructuredSelection) event.getSelection()).size();
				boolean canEdit = (sels == 1);
				boolean canDel = (sels >= 1);
				buttonDefineEdit.setEnabled(canEdit);
				buttonDefineDel.setEnabled(canDel);
			}
		});

	}

	/**
	 * Creates a selection adapter that changes the sorting order and sorting column
	 * of the table.
	 */
	private SelectionAdapter createSelectionAdapter(final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setSortColumn(index);
				int dir = comparator.getSortDirection();
				tableViewer.getTable().setSortDirection(dir);
				tableViewer.getTable().setSortColumn(column);
				tableViewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * Creates a table viewer column for the table.
	 */
	private TableViewerColumn createTableViewerColumn(final TableViewer viewer, String title, int colWidth,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(colWidth);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(createSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	private TableViewer createViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		createColumns(parent, viewer);

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new MesonProjectOptionTableContentProvider());
		viewer.setLabelProvider(new MesonProjectOptionLabelProvider());

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		// gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
		return viewer;
	}

	////////////////////////////////////////////////////////////////////
	// inner classes
	////////////////////////////////////////////////////////////////////

	private void handleDefineAddButton(TableViewer tableViewer) {
		final Shell shell = tableViewer.getControl().getShell();
		AddMesonProjectOptionDialog dlg = new AddMesonProjectOptionDialog(shell, cfgd, null);
		if (dlg.open() == Window.OK) {
			ProjectOption mesonDefine = dlg.getProjectOption();
			@SuppressWarnings("unchecked")
			ArrayList<ProjectOption> defines = (ArrayList<ProjectOption>) tableViewer.getInput();
			defines.add(mesonDefine);
			tableViewer.add(mesonDefine); // updates the display
		}
	}

	private void handleDefineDelButton(TableViewer tableViewer) {
		final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		final Shell shell = tableViewer.getControl().getShell();
		if (MessageDialog.openQuestion(shell, Messages.ProjectOptionsViewer_4,
				Messages.ProjectOptionsViewer_5)) {
			@SuppressWarnings("unchecked")
			ArrayList<ProjectOption> defines = (ArrayList<ProjectOption>) tableViewer.getInput();
			defines.removeAll(selection.toList());
			tableViewer.remove(selection.toArray());// updates the display
		}
	}

	private void handleDefineEditButton(TableViewer tableViewer) {
		final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		if (selection.size() == 1) {
			Object mesonDefine = selection.getFirstElement();
			// edit the selected variable in-place..
			final Shell shell = tableViewer.getControl().getShell();
			AddMesonProjectOptionDialog dlg = new AddMesonProjectOptionDialog(shell, cfgd, (ProjectOption) mesonDefine);
			if (dlg.open() == Window.OK) {
				tableViewer.update(mesonDefine, null); // updates the display
			}
		}
	}
}
