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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IBadDirective;
import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IComment;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IEmptyLine;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IParent;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.ITargetRule;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.core.makefile.gnu.ITerminal;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.icemoon.cdt.ninja.core.IRuleDef;
import org.icemoon.cdt.ninja.core.ISubninja;
import org.icemoon.cdt.ninja.ui.internal.NinjaUIPlugin;

/**
 * NinjaContentOutlinePage
 */
public class NinjaContentOutlinePage extends ContentOutlinePage {
	private class MakefileContentProvider implements ITreeContentProvider {
		protected boolean showMacroDefinition = true;
		protected boolean showTargetRule = true;
		protected boolean showInferenceRule = true;
		protected boolean showIncludeChildren = false;

		protected IMakefile makefile;
		protected IMakefile nullMakefile = new NullMakefile();

		@Override
		public Object[] getChildren(Object element) {
			if (element == fInput) {
				return getElements(makefile);
			} else if (element instanceof IDirective) {
				return getElements(element);
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IMakefile) {
				return fInput;
			} else if (element instanceof IDirective) {
				return ((IDirective) element).getParent();
			}
			return fInput;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element == fInput) {
				return true;
			} else if (element instanceof IParent) {
				// Do not drill down in includes.
				if (isIncluded(element) && !showIncludeChildren) {
					return false;
				}
				return true;
			}
			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			IDirective[] directives;
			if (inputElement == fInput) {
				directives = makefile.getDirectives();
			} else if (inputElement instanceof IRule) {
				directives = ((IRule) inputElement).getCommands();
			} else if (inputElement instanceof IParent) {
				if (isIncluded(inputElement) && !showIncludeChildren) {
					directives = new IDirective[0];
				} else {
					directives = ((IParent) inputElement).getDirectives();
				}
			} else {
				directives = new IDirective[0];
			}
			List<IDirective> list = new ArrayList<IDirective>(directives.length);
			for (IDirective directive : directives) {
				if (showMacroDefinition && directive instanceof IMacroDefinition) {
					list.add(directive);
				} else if (showInferenceRule && directive instanceof IInferenceRule) {
					list.add(directive);
				} else if (showTargetRule && directive instanceof ITargetRule) {
					list.add(directive);
				} else {
					boolean irrelevant = (directive instanceof IComment || directive instanceof IEmptyLine
							|| directive instanceof ITerminal);
					if (!irrelevant) {
						list.add(directive);
					}
				}
			}
			return list.toArray();
		}

		private boolean isIncluded(Object inputElement) {
			return inputElement instanceof IInclude || inputElement instanceof ISubninja;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != null) {
				makefile = nullMakefile;
			}

			if (newInput != null) {
				IWorkingCopyManager manager = NinjaUIPlugin.getDefault().getWorkingCopyManager();
				makefile = manager.getWorkingCopy((IEditorInput) newInput);
				if (makefile == null) {
					makefile = nullMakefile;
				}
			}
		}
	}

	private class NinjaLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element instanceof IRuleDef) {
				return NinjaUIImages.getImage(NinjaUIImages.IMG_OBJS_INFERENCE_RULE);
			} else if (element instanceof ITargetRule) {
				return NinjaUIImages.getImage(NinjaUIImages.IMG_OBJS_TARGET);
			} else if (element instanceof IInferenceRule) {
				return NinjaUIImages.getImage(NinjaUIImages.IMG_OBJS_INFERENCE_RULE);
			} else if (element instanceof IMacroDefinition) {
				return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_VARIABLE);
			} else if (element instanceof ICommand) {
				return NinjaUIImages.getImage(NinjaUIImages.IMG_OBJS_COMMAND);
			} else if (element instanceof ISubninja) {
				return NinjaUIImages.getImage(NinjaUIImages.IMG_ETOOL_SUBNINJA);
			} else if (element instanceof IInclude) {
				return NinjaUIImages.getImage(NinjaUIImages.IMG_ETOOL_INCLUDE);
			} else if (element instanceof IBadDirective) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			} else if (element instanceof IParent) {
				return NinjaUIImages.getImage(NinjaUIImages.IMG_OBJS_RELATION);
			}
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			String name;
			if (element instanceof IRule) {
				name = ((IRule) element).getTarget().toString().trim();
			} else if (element instanceof IMacroDefinition) {
				name = ((IMacroDefinition) element).getName().trim();
			} else {
				name = super.getText(element);
			}
			if (name != null) {
				name = name.trim();
				if (name.length() > 25) {
					name = name.substring(0, 25) + " ..."; //$NON-NLS-1$
				}
			}
			return name;
		}
	}

	protected NinjaFileEditor fEditor;
	protected Object fInput;
	protected AddBuildTargetAction fAddBuildTargetAction;
	protected OpenIncludeAction fOpenIncludeAction;
	protected OpenSubninjaAction fOpenSubninjaAction;

	public NinjaContentOutlinePage(NinjaFileEditor editor) {
		super();
		fEditor = editor;
		fAddBuildTargetAction = new AddBuildTargetAction(this);
		fOpenIncludeAction = new OpenIncludeAction(this);
		fOpenSubninjaAction = new OpenSubninjaAction(this);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new MakefileContentProvider());
		viewer.setLabelProvider(new NinjaLabelProvider());
		if (fInput != null) {
			viewer.setInput(fInput);
		}

		MenuManager manager = new MenuManager("#MakefileOutlinerContext"); //$NON-NLS-1$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		Control tree = viewer.getControl();
		Menu menu = manager.createContextMenu(tree);
		tree.setMenu(menu);

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (fOpenIncludeAction != null && fOpenIncludeAction.canActionBeAdded(getSelection())) {
					fOpenIncludeAction.run();
				} else if (fOpenSubninjaAction != null && fOpenSubninjaAction.canActionBeAdded(getSelection())) {
					fOpenSubninjaAction.run();
				}
			}
		});

		IPageSite site = getSite();
		site.registerContextMenu(NinjaUIPlugin.getPluginId() + ".outline", manager, viewer); //$NON-NLS-1$
		site.setSelectionProvider(viewer);
	}

	/**
	 * called to create the context menu of the outline
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		if (fOpenIncludeAction.canActionBeAdded(getSelection())) {
			menu.add(fOpenIncludeAction);
		}
		if (fOpenSubninjaAction.canActionBeAdded(getSelection())) {
			menu.add(fOpenSubninjaAction);
		}
		if (fAddBuildTargetAction.canActionBeAdded(getSelection())) {
			menu.add(fAddBuildTargetAction);
		}
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));//$NON-NLS-1$
	}

	/**
	 * Sets the input of the outline page
	 */
	public void setInput(Object input) {
		fInput = input;
		update();
	}

	public Object getInput() {
		return fInput;
	}

	/**
	 * Updates the outline page.
	 */
	public void update() {
		final TreeViewer viewer = getTreeViewer();

		if (viewer != null) {
			final Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!control.isDisposed()) {
							control.setRedraw(false);
							viewer.setInput(fInput);
							viewer.expandAll();
							control.setRedraw(true);
						}
					}
				});
			}
		}
	}

	@Override
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		IToolBarManager toolBarManager = actionBars.getToolBarManager();

		LexicalSortingAction action = new LexicalSortingAction(getTreeViewer());
		toolBarManager.add(action);
	}

}
