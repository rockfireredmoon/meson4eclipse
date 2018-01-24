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
 *     Anton Leherbauer (Wind River Systems)
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.editor;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class NinjaSourceConfiguration extends TextSourceViewerConfiguration {
	private ColorManager fColorManager;
	NinjaCodeScanner fCodeScanner;
	private NinjaFileEditor fEditor;
	private SingleTokenScanner fCommentScanner;

	/**
	 * Single token scanner.
	 */
	static class SingleTokenScanner extends AbstractMakefileCodeScanner {
		private final String[] fProperties;

		public SingleTokenScanner(String property) {
			fProperties = new String[] { property };
			initialize();
		}

		@Override
		protected List<IRule> createRules() {
			setDefaultReturnToken(getToken(fProperties[0]));
			return null;
		}

		@Override
		protected String[] getTokenProperties() {
			return fProperties;
		}
	}

	/**
	 * Constructor for MakeConfiguration
	 */
	public NinjaSourceConfiguration(IPreferenceStore preferenceStore, NinjaFileEditor editor) {
		super(preferenceStore);
		fEditor = editor;
		fColorManager = ColorManager.getDefault();
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer v) {
		return NinjaPartitionScanner.MAKE_PARTITIONS;

	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (fEditor != null && fEditor.isEditable()) {
			ContentAssistant assistant = new ContentAssistant();
			assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
			assistant.setContentAssistProcessor(new NinjaCompletionProcessor(fEditor), IDocument.DEFAULT_CONTENT_TYPE);
			assistant.setContentAssistProcessor(new NinjaCompletionProcessor(fEditor),
					NinjaPartitionScanner.MAKEFILE_COMMENT_PARTITION);

			assistant.enableAutoActivation(true);
			assistant.setAutoActivationDelay(500);

			assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
			assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
			assistant.setContextInformationPopupBackground(fColorManager.getColor(new RGB(255, 255, 255)));
			assistant.setInformationControlCreator(new IInformationControlCreator() {
				@Override
				public IInformationControl createInformationControl(Shell parent) {
					return new DefaultInformationControl(parent, false);
				}
			});

			return assistant;
		}
		return null;
	}

	protected NinjaCodeScanner getCodeScanner() {
		if (null == fCodeScanner) {
			fCodeScanner = new NinjaCodeScanner();
		}
		return fCodeScanner;
	}

	protected ITokenScanner getCommentScanner() {
		if (null == fCommentScanner) {
			fCommentScanner = new SingleTokenScanner(ColorManager.NINJA_COMMENT_COLOR);
		}
		return fCommentScanner;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer v) {

		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(v));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getCommentScanner());
		reconciler.setDamager(dr, NinjaPartitionScanner.MAKEFILE_COMMENT_PARTITION);
		reconciler.setRepairer(dr, NinjaPartitionScanner.MAKEFILE_COMMENT_PARTITION);
		return reconciler;
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return NinjaDocumentSetupParticipant.MAKEFILE_PARTITIONING;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fEditor != null && fEditor.isEditable()) {
			MonoReconciler reconciler = new MonoReconciler(new NinjaReconcilingStrategy(fEditor), false);
			reconciler.setDelay(1000);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			return reconciler;
		}
		return null;
	}

	@Override
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "#" }; //$NON-NLS-1$
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (fEditor != null) {
			return new NinjaTextHover(fEditor);
		}
		return super.getTextHover(sourceViewer, contentType);
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		if (fEditor != null) {
			return new NinjaAnnotationHover(fEditor);
		}
		return super.getAnnotationHover(sourceViewer);
	}

	/**
	 * @return <code>true</code> if the given property change event affects the code
	 *         coloring
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		if (fCodeScanner != null && fCodeScanner.affectsBehavior(event)) {
			return true;
		}
		if (fCommentScanner != null && fCommentScanner.affectsBehavior(event)) {
			return true;
		}
		return false;
	}

	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (fCodeScanner != null) {
			fCodeScanner.adaptToPreferenceChange(event);
		}
		if (fCommentScanner != null) {
			fCommentScanner.adaptToPreferenceChange(event);
		}
	}

}
