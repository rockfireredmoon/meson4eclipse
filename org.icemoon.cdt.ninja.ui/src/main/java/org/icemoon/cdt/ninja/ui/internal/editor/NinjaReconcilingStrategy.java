/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - Bug 316502
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/

package org.icemoon.cdt.ninja.ui.internal.editor;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.icemoon.cdt.ninja.ui.internal.NinjaUIPlugin;

public class NinjaReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private ITextEditor fEditor;
	private IWorkingCopyManager fManager;
	private IDocumentProvider fDocumentProvider;
	private NinjaContentOutlinePage fOutliner;
	private IReconcilingParticipant fMakefileReconcilingParticipant;

	public NinjaReconcilingStrategy(NinjaFileEditor editor) {
		fOutliner = editor.getOutlinePage();
		fEditor = editor;
		fManager = NinjaUIPlugin.getDefault().getWorkingCopyManager();
		fDocumentProvider = NinjaUIPlugin.getDefault().getNinjaDocumentProvider();
		if (fEditor instanceof IReconcilingParticipant) {
			fMakefileReconcilingParticipant = (IReconcilingParticipant) fEditor;
		}

	}

	/**
	 * @see IReconcilingStrategy#setDocument(IDocument)
	 */
	@Override
	public void setDocument(IDocument document) {
	}

	/**
	 * @see IReconcilingStrategy#reconcile(IRegion)
	 */
	@Override
	public void reconcile(IRegion region) {
		reconcile();
	}

	/**
	 * @see IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
	 */
	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		assert false : "This is a non-incremental reconciler"; //$NON-NLS-1$
	}

	private void reconcile() {
		try {
			IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
			if (makefile != null) {
				String content = fDocumentProvider.getDocument(fEditor.getEditorInput()).get();
				StringReader reader = new StringReader(content);
				try {
					makefile.parse(makefile.getFileURI(), reader);
				} catch (IOException e) {
				}

				fOutliner.update();
			}
		} finally {
			try {
				if (fMakefileReconcilingParticipant != null) {
					fMakefileReconcilingParticipant.reconciled();
				}
			} finally {
				//
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#
	 * setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// no use for a progress monitor at the moment
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#
	 * initialReconcile()
	 */
	@Override
	public void initialReconcile() {
		// no need to reconcile initially
		// reconcile();
	}
}
