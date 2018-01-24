/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.icemoon.cdt.meson.ui.internal;

import org.eclipse.jface.wizard.Wizard;
import org.icemoon.cdt.meson.core.IMesonCrossCompileFile;

/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class NewMesonToolChainFileWizard extends Wizard {

	private IMesonCrossCompileFile newFile;
	private NewMesonToolChainFilePage page;

	@Override
	public void addPages() {
		page = new NewMesonToolChainFilePage();
		addPage(page);
	}

	public IMesonCrossCompileFile getNewFile() {
		return newFile;
	}

	@Override
	public boolean performFinish() {
		newFile = page.getNewFile();
		return true;
	}

}
