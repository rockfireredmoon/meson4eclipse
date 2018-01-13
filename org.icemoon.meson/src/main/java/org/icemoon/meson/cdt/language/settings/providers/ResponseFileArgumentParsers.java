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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.icemoon.meson.cdt.language.settings.providers.ToolArgumentParsers.NameOptionMatcher;

/**
 * Various response-file argument parser implementations.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
class ResponseFileArgumentParsers {
	/**
	 * matches a response file name with quoted file name. Name in matcher group 2
	 */
	private static final String REGEX_QUOTED_FILE = "\\s*([\"'])(.+?)\\1";
	/**
	 * matches a response file name with unquoted file name. Name in matcher group 1
	 */
	private static final String REGEX_UNQUOTED_FILE = "\\s*([^\\s]+)";

	/**
	 * Handles a response file argument that starts with the '@' character.
	 */
	static class At implements IResponseFileArgumentParser {
		private static final NameOptionMatcher[] optionMatchers = {
				/* unquoted directory */
				new NameOptionMatcher("@" + REGEX_UNQUOTED_FILE, 1),
				/* quoted directory */
				new NameOptionMatcher("@" + REGEX_QUOTED_FILE, 2) };

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.icemoon.meson.cdt.language.settings.providers.IResponseFileArgumentParser
		 * #process(org.icemoon.meson.cdt.language.settings.providers.IParserHandler,
		 * java.lang.String)
		 */
		@Override
		public int process(IParserHandler parserHandler, String argsLine, IPath project) {
			for (NameOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(argsLine);
				if (matcher.lookingAt()) {
					String fname = matcher.group(oMatcher.nameGroup);
					IPath path = Path.fromOSString(fname);
					if (!path.isAbsolute()) {
						// relative path, prepend CWD
						fname = parserHandler.getCompilerWorkingDirectory().append(path).toOSString();
					}

					// parse file
					java.nio.file.Path fpath = Paths.get(fname);
					try {
						String args2 = new String(Files.readAllBytes(fpath));
						parserHandler.parseArguments(args2, project);
					} catch (IOException e) {
						// swallow exception for now
						e.printStackTrace();
					}
					return matcher.end();
				}
			}
			return 0;// no input consumed
		}

	}

}
