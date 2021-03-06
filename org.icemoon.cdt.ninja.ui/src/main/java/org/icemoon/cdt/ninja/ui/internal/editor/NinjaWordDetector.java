/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
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

import org.eclipse.jface.text.rules.IWordDetector;

public class NinjaWordDetector implements IWordDetector {

	// private static final String correctStartSpecChars = "%*().><"; //$NON-NLS-1$
	private static final String correctStartSpecChars = "%*()><"; //$NON-NLS-1$
	private static final String correctSpecChars = "@$/\\_"; //$NON-NLS-1$

	/**
	 * @see IWordDetector#isWordPart(char)
	 */
	@Override
	public boolean isWordPart(char character) {
		return Character.isLetterOrDigit(character) || (correctSpecChars.indexOf(character) >= 0);
	}

	/**
	 * @see IWordDetector#isWordStart(char)
	 */
	@Override
	public boolean isWordStart(char character) {
		return Character.isLetterOrDigit(character) || (correctStartSpecChars.indexOf(character) >= 0);
	}

}
