/*******************************************************************************
 * Copyright (c) 2002, 2013 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Violaine Batthish (IBM) - bug 270013
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/

package org.icemoon.cdt.ninja.ui.internal.editor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.icemoon.cdt.ninja.core.ISubninja;
import org.icemoon.cdt.ninja.ui.internal.NinjaUIPlugin;

/**
 * OpenSubninjaAction
 */
public class OpenSubninjaAction extends Action {
	ISelectionProvider fSelectionProvider;

	/**
	 * Constructor.
	 */
	public OpenSubninjaAction(ISelectionProvider provider) {
		super(NinjaUIPlugin.getResourceString("OpenSubninjaAction.title")); //$NON-NLS-1$
		setDescription(NinjaUIPlugin.getResourceString("OpenSubninjaAction.description")); //$NON-NLS-1$
		setToolTipText(NinjaUIPlugin.getResourceString("OpenSubninjaAction.tooltip")); //$NON-NLS-1$
		fSelectionProvider = provider;
	}

	@Override
	public void run() {
		ISubninja[] includes = getSubninjaDirective(fSelectionProvider.getSelection());
		if (includes != null) {
			for (ISubninja include : includes) {
				for (IDirective directive : include.getDirectives()) {
					try {
						openInEditor(directive);
					} catch (PartInitException e) {
					}
				}
			}
		}
	}

	public static IEditorPart openInEditor(IDirective directive) throws PartInitException {
		try {
			URI uri = directive.getMakefile().getFileURI();
			IFileStore store = EFS.getStore(uri);

			IFile[] file = NinjaUIPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			if (file.length > 0 && file[0] != null) {
				IWorkbenchPage p = NinjaUIPlugin.getActivePage();
				if (p != null) {
					IEditorPart editorPart = IDE.openEditor(p, file[0], true);
					if (editorPart instanceof NinjaFileEditor) {
						((NinjaFileEditor) editorPart).setSelection(directive, true);
					}
					return editorPart;
				}
			} else {
				// External file
				IEditorInput input = new FileStoreEditorInput(store);
				IWorkbenchPage p = NinjaUIPlugin.getActivePage();
				if (p != null) {
					String editorID = "org.eclipse.cdt.make.editor"; //$NON-NLS-1$
					IEditorPart editorPart = IDE.openEditor(p, input, editorID, true);
					if (editorPart instanceof NinjaFileEditor) {
						((NinjaFileEditor) editorPart).setSelection(directive, true);
					}
					return editorPart;
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}

	ISubninja[] getSubninjaDirective(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			@SuppressWarnings("unchecked")
			List<Object> list = ((IStructuredSelection) sel).toList();
			if (list.size() > 0) {
				List<ISubninja> includes = new ArrayList<ISubninja>(list.size());
				for (Object element : list) {
					if (element instanceof ISubninja) {
						includes.add((ISubninja) element);
					}
				}
				return includes.toArray(new ISubninja[includes.size()]);
			}
		}
		return null;
	}

	public boolean canActionBeAdded(ISelection selection) {
		ISubninja[] includes = getSubninjaDirective(selection);
		return includes != null && includes.length != 0;
	}
}
