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
 *      Emerald Icemoon - Ported to Meson
 *******************************************************************************/
package org.icemoon.cdt.meson.core.ui;

import org.eclipse.cdt.ui.newui.AbstractPage;

/**
 * Page for Meson project settings.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonPropertyPage extends AbstractPage {

	/*-
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#isSingle()
	 */
	@Override
	protected boolean isSingle() {
		return false;
	}

}
