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
 *     Wind River Systems - fix for bugzilla 135150
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.editor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.icemoon.cdt.ninja.ui.internal.NinjaUIPlugin;

/**
 */
public class NinjaFileEditorActionContributor extends TextEditorActionContributor {

	private NinjaFleEditorTogglePresentationAction fTogglePresentation;
	private OpenDeclarationAction fOpenDeclarationAction;
	protected RetargetTextEditorAction fContentAssistProposal;
	protected RetargetTextEditorAction fContentAssistTip;

	/**
	 * Constructor for NinjaFileEditorActionContributor.
	 */
	public NinjaFileEditorActionContributor() {
		super();
		ResourceBundle bundle = NinjaUIPlugin.getDefault().getResourceBundle();
		fContentAssistProposal = new RetargetTextEditorAction(bundle, "ContentAssistProposal."); //$NON-NLS-1$
		fContentAssistProposal.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		fContentAssistTip = new RetargetTextEditorAction(bundle, "ContentAssistTip."); //$NON-NLS-1$
		fContentAssistTip.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		fTogglePresentation = new NinjaFleEditorTogglePresentationAction();
		fOpenDeclarationAction = new OpenDeclarationAction();
		fOpenDeclarationAction.setActionDefinitionId(INinjaEditorActionDefinitionIds.OPEN_DECLARATION);
	}

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		doSetActiveEditor(targetEditor);
	}

	private void doSetActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		ITextEditor editor = null;
		if (part instanceof ITextEditor) {
			editor = (ITextEditor) part;
		}

		fContentAssistProposal.setAction(getAction(editor, "ContentAssistProposal")); //$NON-NLS-1$
		fContentAssistTip.setAction(getAction(editor, "ContentAssistTip")); //$NON-NLS-1$

		fTogglePresentation.setEditor(editor);
		fTogglePresentation.update();

		fOpenDeclarationAction.setEditor(editor);
		fOpenDeclarationAction.update();
	}

	@Override
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#init(IActionBars)
	 */
	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		IMenuManager menuManager = bars.getMenuManager();
		IMenuManager editMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.add(new Separator());
			editMenu.add(fContentAssistProposal);
			editMenu.add(fContentAssistTip);
			editMenu.add(fOpenDeclarationAction);
		}

		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY,
				fTogglePresentation);
		// there is a global action in the toolbar, that is retargeted,
		// there is no need to add another one.
		// IToolBarManager toolBarManager = bars.getToolBarManager();
		// if (toolBarManager != null) {
		// toolBarManager.add(new Separator());
		// toolBarManager.add(fTogglePresentation);
		// }
	}

}
