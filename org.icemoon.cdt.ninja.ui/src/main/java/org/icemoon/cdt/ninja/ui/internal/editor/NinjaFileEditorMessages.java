/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Patrick Hofer - Bug 326265
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.editor;

import org.eclipse.osgi.util.NLS;

public final class NinjaFileEditorMessages extends NLS {
	public static String MakefileEditor_menu_folding;
	public static String ToggleComment_error_title;
	public static String ToggleComment_error_message;

	static {
		NLS.initializeMessages(NinjaFileEditorMessages.class.getName(), NinjaFileEditorMessages.class);
	}

	// Do not instantiate
	private NinjaFileEditorMessages() {
	}
}