/*******************************************************************************
 * Copyright (c) 2014-2017 Martin Weber.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Martin Weber - Initial implementation
 *      Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.preferences;

import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.core.runtime.CoreException;

/**
 * Page for Meson project settings.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class NinjaPropertyPage extends AbstractPage {

	/*-
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#isSingle()
	 */
	@Override
	protected boolean isSingle() {
		return false;
	}

	@Override
	protected boolean showsConfig() {
		try {
			return isForProject() && getProject().getNature(ManagedCProjectNature.MNG_NATURE_ID) != null;
		} catch (CoreException e) {
			return false;
		}
	}

}
