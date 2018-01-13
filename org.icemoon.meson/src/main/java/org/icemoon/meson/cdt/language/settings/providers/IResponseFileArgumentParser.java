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
 * Parses a 'response file' tool argument and its content.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
interface IResponseFileArgumentParser {

	/**
	 * Detects whether the first argument on the given {@code argsLine} argument
	 * denotes a response file and parses that file.
	 *
	 * @param parserHandler
	 *            the handler to parse the arguments in the response-file`s content
	 * @param argsLine
	 *            the arguments passed to the tool, as they appear in the build
	 *            output. Implementers may safely assume that the specified value
	 *            does not contain leading whitespace characters, but trailing WS
	 *            may occur.
	 * @param project project
	 * @return the number of characters from {@code argsLine} that has been
	 *         processed. Return a value of {@code zero} or less, if this tool
	 *         argument parser cannot process the first argument from the input.
	 */
	int process(IParserHandler parserHandler, String argsLine, IPath project);

}
