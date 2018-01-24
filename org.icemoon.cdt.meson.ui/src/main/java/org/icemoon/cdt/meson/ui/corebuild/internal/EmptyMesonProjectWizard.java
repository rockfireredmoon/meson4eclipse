/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *    Emerald Icemoon - Ported to Meson
 *******************************************************************************/

package org.icemoon.cdt.meson.ui.corebuild.internal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.icemoon.cdt.meson.core.corebuild.MesonProjectGenerator;
import org.icemoon.cdt.meson.ui.internal.Messages;


/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class EmptyMesonProjectWizard extends TemplateWizard {

	private WizardNewProjectCreationPage mainPage;

	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), getSelection(),
						new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle(Messages.NewEmptyMesonProjectWizard_Title);
		mainPage.setDescription(Messages.NewEmptyMesonProjectWizard_Description);
		this.addPage(mainPage);
	}

	@Override
	protected IGenerator getGenerator() {
		MesonProjectGenerator generator = new MesonProjectGenerator("templates/empty-meson/manifest.xml"); //$NON-NLS-1$
		generator.setProjectName(mainPage.getProjectName());
		if (!mainPage.useDefaults()) {
			generator.setLocationURI(mainPage.getLocationURI());
		}
		return generator;
	}

}
