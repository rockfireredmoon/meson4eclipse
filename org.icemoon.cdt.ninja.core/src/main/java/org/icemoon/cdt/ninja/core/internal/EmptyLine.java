/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
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

package org.icemoon.cdt.ninja.core.internal;

import org.eclipse.cdt.make.core.makefile.IEmptyLine;

public class EmptyLine extends Directive implements IEmptyLine {

	final public static char NL = '\n';
	final public static String NL_STRING = "\n"; //$NON-NLS-1$

	public EmptyLine(Directive parent) {
		super(parent);
	}

	@Override
	public String toString() {
		return NL_STRING;
	}

	public boolean equals(IEmptyLine stmt) {
		return true;
	}
}
