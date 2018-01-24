/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.editor;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.icemoon.cdt.ninja.ui.internal.NinjaUIPlugin;

/**
 */
public class NinjaFleEditorTogglePresentationAction extends TextEditorAction {
	/**
	 * Constructor for NinjaFleEditorTogglePresentationAction.
	 */
	public NinjaFleEditorTogglePresentationAction() {
		super(NinjaUIPlugin.getDefault().getResourceBundle(), "TogglePresentation.", null); //$NON-NLS-1$
		setDisabledImageDescriptor(NinjaUIImages.getImageDescriptor(NinjaUIImages.IMG_DTOOL_SEGMENT_EDIT));
		setImageDescriptor(NinjaUIImages.getImageDescriptor(NinjaUIImages.IMG_ETOOL_SEGMENT_EDIT));
		update();
	}

	@Override
	public void run() {
		ITextEditor editor = getTextEditor();
		editor.resetHighlightRange();
		boolean show = editor.showsHighlightRangeOnly();
		setChecked(!show);
		editor.showHighlightRangeOnly(!show);
	}

	@Override
	public void update() {
		setChecked(getTextEditor() != null && getTextEditor().showsHighlightRangeOnly());
		setEnabled(true);
	}

}
