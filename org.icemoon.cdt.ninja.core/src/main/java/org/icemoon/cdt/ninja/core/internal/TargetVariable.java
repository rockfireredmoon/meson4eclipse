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

/**
 * Here is the syntax of a static pattern rule:
 *
 * TARGETS ...: VARIABLE-ASSIGNMENT TARGETS ...: override VARIABLE-ASSIGNMENT
 */
public class TargetVariable extends VariableDefinition {

	boolean override;

	public TargetVariable(Directive parent, String target, String name, StringBuffer value, boolean override,
			int type) {
		super(parent, target, name, value, type);
		this.override = override;
	}

	@Override
	public boolean isOverride() {
		return override;
	}

}
