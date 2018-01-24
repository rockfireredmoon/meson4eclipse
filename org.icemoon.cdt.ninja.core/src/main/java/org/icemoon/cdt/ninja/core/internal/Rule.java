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
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.core.internal;

import org.icemoon.cdt.ninja.core.IRuleDef;
import org.icemoon.cdt.ninja.core.NinjaFileConstants;

public class Rule extends Parent implements IRuleDef {

	String name;

	public Rule(Directive parent, String name) {
		super(parent);
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(NinjaFileConstants.RULEDEF);
		sb.append(' ').append(name);
		return sb.toString();
	}

	public String getName() {
		return name;
	}
}
