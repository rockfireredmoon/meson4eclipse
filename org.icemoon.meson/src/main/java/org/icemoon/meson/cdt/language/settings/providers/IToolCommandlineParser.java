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

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.runtime.IPath;

/**
 * Parses the command-line produced by a specific tool invocation and detects
 * LanguageSettings.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
interface IToolCommandlineParser {
	/**
	 * Parses all arguments given to the tool.
	 *
	 * @param cwd
	 *            the current working directory of the compiler at the time of its
	 *            invocation
	 * @param args
	 *            the command line arguments to process
	 * @param project project
	 * @return the language setting entries produced or {@code null} or an empty
	 *         list if no entries where produced
	 * @throws NullPointerException
	 *             if any of the arguments is <code>null</code>
	 */
	public List<ICLanguageSettingEntry> processArgs(IPath cwd, String args, IPath project);

	/**
	 * Gets the language ID of the language that the tool compiles.
	 *
	 * @return the language ID, {@code null} is allowed if this parser does not
	 *         produce any {@link ICLanguageSettingEntry language settings entries}
	 */
	public String getLanguageId();
}