/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.editor;

import java.net.URI;
import java.util.Arrays;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.icemoon.cdt.ninja.ui.internal.NinjaUIPlugin;

public class OpenDeclarationAction extends TextEditorAction {
	public OpenDeclarationAction() {
		this(null);
	}

	public OpenDeclarationAction(ITextEditor editor) {
		super(NinjaUIPlugin.getDefault().getResourceBundle(), "OpenDeclarationAction.", editor); //$NON-NLS-1$
	}

	@Override
	public void run() {
		ITextEditor editor = getTextEditor();
		if (editor == null) {
			return;
		}
		ISelectionProvider provider = editor.getSelectionProvider();
		if (provider == null) {
			return;
		}
		IDirective[] directives = null;
		IWorkingCopyManager fManager = NinjaUIPlugin.getDefault().getWorkingCopyManager();
		IMakefile makefile = fManager.getWorkingCopy(editor.getEditorInput());
		if (makefile != null) {
			IDocumentProvider prov = editor.getDocumentProvider();
			IDocument doc = prov.getDocument(editor.getEditorInput());
			try {
				ITextSelection textSelection = (ITextSelection) provider.getSelection();
				int offset = textSelection.getOffset();
				WordPartDetector wordPart = new WordPartDetector(doc, offset);
				String name = wordPart.getName();
				if (wordPart.isMacro()) {
					directives = makefile.getMacroDefinitions(name);
				} else if (wordPart.isFunctionCall()) {
					directives = makefile.getMacroDefinitions(wordPart.getName());
				} else if (wordPart.isIncludeDirective()) {
					String incFile = wordPart.getName();
					incFile = makefile.expandString(incFile, true);
					for (IDirective dir : makefile.getDirectives()) {
						if (dir instanceof IInclude) {
							IInclude includeDirective = (IInclude) dir;
							if (Arrays.asList(((IInclude) dir).getFilenames()).contains(incFile)) {
								IDirective[] includedMakefiles = includeDirective.getDirectives();
								for (IDirective includedMakefileDir : includedMakefiles) {
									if (includedMakefileDir instanceof IMakefile) {
										IMakefile includedMakefile = (IMakefile) includedMakefileDir;
										URI uri = includedMakefile.getFileURI();
										// endsWith() is potentially inaccurate but ISubninja does not provide better way
										if (uri != null && uri.toString().endsWith(incFile)) {
											directives = new IDirective[1];
											directives[0] = includedMakefileDir;
											break;
										}
									}
								}
							}
						}
					}
				} else {
					directives = makefile.getTargetRules(name);
				}
				if (directives != null && directives.length > 0) {
					OpenIncludeAction.openInEditor(directives[0]);
				}
			} catch (Exception x) {
				//
			}
		}
	}
}
