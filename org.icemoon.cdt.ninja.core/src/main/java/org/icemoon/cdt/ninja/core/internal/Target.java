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

import java.io.File;

import org.eclipse.cdt.make.core.makefile.ITarget;

public class Target implements ITarget {

	String target;

	public Target(String t) {
		target = t;
	}

	@Override
	public String toString() {
		return target;
	}

	public boolean exits() {
		return new File(target).exists();
	}

	public long lastModified() {
		return new File(target).lastModified();
	}
}
