/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Emerald Icemoon - Ported to Meson
 *******************************************************************************/
package org.icemoon.cdt.meson.core;

/**
 * Event occured with Meson ToolChain Files, either added or removed.
 */
public class MesonCrossCompileEvent {

	/**
	 * ToolChain file has been added.
	 */
	public static final int ADDED = 1;

	/**
	 * ToolChain File has been removed.
	 */
	public static final int REMOVED = 2;

	private final int type;
	private final IMesonCrossCompileFile toolChainFile;

	public MesonCrossCompileEvent(int type, IMesonCrossCompileFile toolChainFile) {
		this.type = type;
		this.toolChainFile = toolChainFile;
	}

	public int getType() {
		return type;
	}

	public IMesonCrossCompileFile getToolChainFile() {
		return toolChainFile;
	}

}
