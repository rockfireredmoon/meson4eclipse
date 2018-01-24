/*******************************************************************************
 * Copyright (c) 2015 Martin Weber.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Martin Weber - Initial implementation
 *      Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.core.language.settings.providers;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.runtime.IPath;

/**
 * Converts tool arguments into LanguageSettings objects.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public interface IToolArgumentParser {

	/**
	 * Parses the next command-line argument and extracts all detected
	 * LanguageSettings objects.
	 *
	 * @param returnedEntries
	 *            the buffer that receives the new {@code LanguageSettings}
	 * @param cwd
	 *            the current working directory of the compiler at its invocation
	 * @param argsLine
	 *            the arguments passed to the tool, as they appear in the build
	 *            output. Implementers may safely assume that the specified value
	 *            does not contain leading whitespace characters, but trailing WS
	 *            may occur.
	 * @param project
	 *            TODO
	 * @return the number of characters from {@code argsLine} that has been
	 *         processed. Return a value of {@code zero} or less, if this tool
	 *         argument parser cannot process the first argument from the input.
	 */
	int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine, IPath project);
}