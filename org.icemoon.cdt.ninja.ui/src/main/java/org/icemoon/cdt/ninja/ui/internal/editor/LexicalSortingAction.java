/*******************************************************************************
 * Copyright (c) 2002, 2013 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/

package org.icemoon.cdt.ninja.ui.internal.editor;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.ISpecialRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.icemoon.cdt.ninja.ui.internal.NinjaUIPlugin;

public class LexicalSortingAction extends Action {
	private static final String ACTION_NAME = "LexicalSortingAction"; //$NON-NLS-1$
	private static final String DIALOG_STORE_KEY = ACTION_NAME + ".sort"; //$NON-NLS-1$

	private LexicalMakefileSorter fSorter;
	private TreeViewer fTreeViewer;

	public LexicalSortingAction(TreeViewer treeViewer) {
		super(NinjaUIPlugin.getResourceString(ACTION_NAME + ".label")); //$NON-NLS-1$

		setDescription(NinjaUIPlugin.getResourceString(ACTION_NAME + ".description")); //$NON-NLS-1$
		setToolTipText(NinjaUIPlugin.getResourceString(ACTION_NAME + ".tooltip")); //$NON-NLS-1$
		setDisabledImageDescriptor(NinjaUIImages.getImageDescriptor(NinjaUIImages.IMG_DTOOL_ALPHA_SORTING));
		setImageDescriptor(NinjaUIImages.getImageDescriptor(NinjaUIImages.IMG_ETOOL_ALPHA_SORTING));

		fTreeViewer = treeViewer;
		fSorter = new LexicalMakefileSorter();
		boolean checked = NinjaUIPlugin.getDefault().getDialogSettings().getBoolean(DIALOG_STORE_KEY);
		valueChanged(checked, false);
	}

	@Override
	public void run() {
		valueChanged(isChecked(), true);
	}

	private void valueChanged(boolean on, boolean store) {
		setChecked(on);
		fTreeViewer.setSorter(on ? fSorter : null);

		String key = ACTION_NAME + ".tooltip" + (on ? ".on" : ".off"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setToolTipText(NinjaUIPlugin.getResourceString(key));
		if (store) {
			NinjaUIPlugin.getDefault().getDialogSettings().put(DIALOG_STORE_KEY, on);
		}
	}

	private class LexicalMakefileSorter extends ViewerSorter {
		@Override
		public int category(Object obj) {
			if (obj instanceof IDirective) {
				IDirective directive = (IDirective) obj;
				if (directive instanceof IMacroDefinition) {
					return 0;
				} else if (directive instanceof ISpecialRule) {
					return 1;
				} else if (directive instanceof IInferenceRule) {
					return 2;
				}
			}
			return 3;
		}
	}

}
