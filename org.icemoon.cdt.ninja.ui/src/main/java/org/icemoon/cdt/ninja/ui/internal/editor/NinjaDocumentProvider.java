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

import java.util.Iterator;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org.icemoon.cdt.ninja.core.NinjaPlugin;

public class NinjaDocumentProvider extends TextFileDocumentProvider implements INinjaDocumentProvider {
	IMakefile fMakefile;

	protected class NinjaAnnotationModel extends ResourceMarkerAnnotationModel /* implements IProblemRequestor */ {
		public NinjaAnnotationModel(IResource resource) {
			super(resource);
		}

		public void setMakefile(IMakefile makefile) {
			fMakefile = makefile;
		}
	}

	/**
	 * Remembers a IMakefile for each element.
	 */
	protected class NinjaFileInfo extends FileInfo {
		public IMakefile fCopy;
	}

	public NinjaDocumentProvider() {
		IDocumentProvider provider = new TextFileDocumentProvider(new NinjaStorageDocumentProvider());
		provider = new ForwardingDocumentProvider(NinjaDocumentSetupParticipant.MAKEFILE_PARTITIONING,
				new NinjaDocumentSetupParticipant(), provider);
		setParentDocumentProvider(provider);
	}

	/**
	 */
	private IMakefile createMakefile(IFile file) throws CoreException {
		if (file.exists()) {
			return NinjaPlugin.createMakefile(file.getLocationURI(), null);
		}
		return null;
	}

	@Override
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return new NinjaAnnotationModel(file);
	}

	@Override
	protected FileInfo createFileInfo(Object element) throws CoreException {
		if (!(element instanceof IFileEditorInput)) {
			return null;
		}

		IFileEditorInput input = (IFileEditorInput) element;
		IMakefile original = createMakefile(input.getFile());
		if (original == null) {
			return null;
		}

		FileInfo info = super.createFileInfo(element);
		if (!(info instanceof NinjaFileInfo)) {
			return null;
		}

		NinjaFileInfo makefileInfo = (NinjaFileInfo) info;
		setUpSynchronization(makefileInfo);

		makefileInfo.fCopy = original;

		if (makefileInfo.fModel instanceof NinjaAnnotationModel) {
			NinjaAnnotationModel model = (NinjaAnnotationModel) makefileInfo.fModel;
			model.setMakefile(makefileInfo.fCopy);
		}
		return makefileInfo;
	}

	@Override
	protected void disposeFileInfo(Object element, FileInfo info) {
		if (info instanceof NinjaFileInfo) {
			NinjaFileInfo makefileInfo = (NinjaFileInfo) info;
			if (makefileInfo.fCopy != null) {
				makefileInfo.fCopy = null;
			}
		}
		super.disposeFileInfo(element, info);
	}

	@Override
	protected FileInfo createEmptyFileInfo() {
		return new NinjaFileInfo();
	}

	@Override
	public IMakefile getWorkingCopy(Object element) {
		FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof NinjaFileInfo) {
			NinjaFileInfo info = (NinjaFileInfo) fileInfo;
			return info.fCopy;
		}
		return null;
	}

	@Override
	public void shutdown() {
		Iterator<?> e = getConnectedElementsIterator();
		while (e.hasNext()) {
			disconnect(e.next());
		}
	}

	@Override
	protected DocumentProviderOperation createSaveOperation(Object element, IDocument document, boolean overwrite)
			throws CoreException {
		if (!(element instanceof IFileEditorInput)) {
			return null;
		}
		return super.createSaveOperation(element, document, overwrite);
	}
}
