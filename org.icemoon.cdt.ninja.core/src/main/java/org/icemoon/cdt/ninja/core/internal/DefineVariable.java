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

public class DefineVariable extends VariableDefinition {

	public DefineVariable(Directive parent, String name, StringBuffer value) {
		super(parent, name, value);
	}

	@Override
	public boolean isMultiLine() {
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName()).append(" = ");
		sb.append(getValue());
		return sb.toString();
	}
}
