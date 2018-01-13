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

package org.icemoon.meson.cdt.language.settings.providers;

import org.eclipse.core.runtime.IPath;

/**
 * Handles parsing of command-line arguments.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
interface IParserHandler {

	/**
	 * Parses the given String with the first parser that can handle the first
	 * argument on the command-line.
	 *
	 * @param args
	 *            the command line arguments to process
	 * @param project projecct
	 */
	void parseArguments(String args, IPath project);

	/**
	 * Gets the current working directory of the compiler at the time of its
	 * invocation.
	 */
	IPath getCompilerWorkingDirectory();
}
