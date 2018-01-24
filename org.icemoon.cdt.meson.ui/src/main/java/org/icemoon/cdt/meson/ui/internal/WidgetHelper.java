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

import java.util.Arrays;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Static methods for unified creation of widgets.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
/* package */class WidgetHelper {

	/**
	 * Creates a button.
	 *
	 * @param parent
	 * @param text
	 *            button text
	 * @param enabled
	 *            whether the button should be initially enabled
	 */
	static Button createButton(Composite parent, String text, boolean enabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		if (!enabled)
			button.setEnabled(false);
		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		// gd.minimumWidth = width;
		button.setLayoutData(gd);
		return button;
	}

	/**
	 * Creates a checkbox button.
	 *
	 * @param parent
	 * @param horizontalAlignment
	 *            how control will be positioned horizontally within a cell of the
	 *            parent's grid layout, one of: SWT.BEGINNING (or SWT.LEFT),
	 *            SWT.CENTER, SWT.END (or SWT.RIGHT), or SWT.FILL
	 * @param horizontalSpan
	 *            number of column cells in the parent's grid layout that the
	 *            control will take up.
	 * @param text
	 *            text to display on the checkbox
	 */
	static Button createCheckbox(Composite parent, int horizontalAlignment, int horizontalSpan, String text) {
		Button b = new Button(parent, SWT.CHECK);
		b.setText(text);
		GridData gd = new GridData(horizontalAlignment, SWT.CENTER, false, false);
		gd.horizontalSpan = horizontalSpan;
		b.setLayoutData(gd);
		return b;
	}

	/**
	 * Creates a combobox.
	 *
	 * @param parent
	 * @param selection
	 *            selection
	 * @param enabled
	 *            whether the button should be initially enabled
	 * @param options
	 */
	@SuppressWarnings("unchecked")
	static <T> ComboViewer createCombo(Composite parent, int horizontalAlignment, int horizontalSpan, T selection,
			boolean enabled, T... options) {
		ComboViewer combobox = new ComboViewer(parent, SWT.NONE);
		combobox.setContentProvider(ArrayContentProvider.getInstance());
		combobox.setInput(options);
		int idx = selection == null ? -1 : Arrays.asList(options).indexOf(selection);
		if (idx != -1)
			combobox.getCombo().select(idx);
		if (!enabled)
			combobox.getCombo().setEnabled(false);
		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		// gd.minimumWidth = width;
		combobox.getCombo().setLayoutData(gd);
		return combobox;
	}

	/**
	 * Creates a group with a grid layout.
	 *
	 * @param parent
	 * @param horizontalAlignment
	 *            how control will be positioned horizontally within a cell of the
	 *            parent's grid layout, one of: SWT.BEGINNING (or SWT.LEFT),
	 *            SWT.CENTER, SWT.END (or SWT.RIGHT), or SWT.FILL
	 * @param horizontalSpan
	 *            number of column cells in the parent's grid layout that the
	 *            control will take up.
	 * @param text
	 *            title text to display on the group
	 * @param numColumns
	 *            the number of columns in the grid inside the group
	 */
	static Group createGroup(Composite parent, int horizontalAlignment, int horizontalSpan, String text,
			int numColumns) {
		Group gr = new Group(parent, SWT.NONE);
		gr.setLayout(new GridLayout(numColumns, false));
		gr.setText(text);
		GridData gd = new GridData(horizontalAlignment, SWT.CENTER, true, false);
		gd.horizontalSpan = horizontalSpan;
		gr.setLayoutData(gd);
		return gr;
	}

	private WidgetHelper() {
		// nothing to instantiate
	}

}
