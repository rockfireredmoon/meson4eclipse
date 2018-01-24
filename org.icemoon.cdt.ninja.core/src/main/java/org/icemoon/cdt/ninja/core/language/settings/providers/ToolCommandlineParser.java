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
package org.icemoon.cdt.ninja.core.language.settings.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.runtime.IPath;

/**
 * Parses the build output produced by a specific tool invocation and detects
 * LanguageSettings.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
class ToolCommandlineParser implements IToolCommandlineParser {

	private final IToolArgumentParser[] argumentParsers;
	private final String languageID;
	private final IResponseFileArgumentParser responseFileArgumentParser;

	/** gathers all entries */
	private List<ICLanguageSettingEntry> entries;

	private IPath cwd;

	/**
	 * @param languageID
	 *            the language ID of the language that the tool compiles
	 * @param responseFileArgumentParser
	 *            the parsers for the response-file command-line argument for the
	 *            tool of <code>null</code> if the tool does not recognize a
	 *            response-file argument
	 * @param argumentParsers
	 *            the parsers for the command line arguments of of interest for the
	 *            tool
	 * @throws NullPointerException
	 *             if {@code argumentParsers} is {@code null}
	 */
	public ToolCommandlineParser(String languageID, IResponseFileArgumentParser responseFileArgumentParser,
			IToolArgumentParser... argumentParsers) {
		this.responseFileArgumentParser = responseFileArgumentParser;
		this.languageID = Objects.requireNonNull(languageID, "languageID");
		if (argumentParsers == null) {
			throw new NullPointerException("argumentParsers");
		}
		this.argumentParsers = argumentParsers;
	}

	@Override
	public List<ICLanguageSettingEntry> processArgs(IPath cwd, String args, IPath project) {
		this.entries = new ArrayList<>();
		this.cwd = Objects.requireNonNull(cwd, "cwd");

		ParserHandler ph = new ParserHandler();
		ph.parseArguments(responseFileArgumentParser, args, project);
		return entries;
	}

	/*-
	 * @see org.icemoon.cdt.meson.ui.language.settings.providers.MesonBuildOutputParser.IBuildOutputToolParser#getLanguageId()
	 */
	@Override
	public String getLanguageId() {
		return languageID;
	}

	@Override
	public String toString() {
		return "[languageID=" + this.languageID + ", argumentParsers=" + Arrays.toString(this.argumentParsers) + "]";
	}

	/**
	 * @param buildOutput
	 *            the command line arguments to process
	 * @return the number of characters consumed
	 */
	private static int skipArgument(String buildOutput) {
		int consumed;

		// (blindly) advance to next whitespace
		if ((consumed = buildOutput.indexOf(' ')) != -1) {
			return consumed;
		} else {
			// non-option arg, may be a file name
			// for now, we just clear/skip the output
			return buildOutput.length();
		}
	}

	/**
	 * Handles parsing of command-line arguments.
	 *
	 * @author Martin Weber @author Emerald Icemoon
	 */
	private class ParserHandler implements IParserHandler {

		/**
		 * @param responseFileArgumentParser
		 * @param args
		 *            the command line arguments to process
		 */
		private void parseArguments(IResponseFileArgumentParser responseFileArgumentParser, String args,
				IPath project) {
			// eat buildOutput string argument by argument..
			while (!(args = args.trim()).isEmpty()) {
				boolean argParsed = false;
				int consumed;
				// parse with first parser that can handle the first argument on the
				// command-line
				for (IToolArgumentParser tap : argumentParsers) {
					consumed = tap.processArgument(entries, cwd, args, project);
					if (consumed > 0) {
						args = args.substring(consumed);
						argParsed = true;
					}
				}

				// try response file
				if (responseFileArgumentParser != null) {
					consumed = responseFileArgumentParser.process(this, args, project);
					if (consumed > 0) {
						args = args.substring(consumed);
						argParsed = true;
					}
				}
				if (!argParsed && !args.isEmpty()) {
					// tried all parsers, argument is still not parsed,
					// skip argument
					consumed = skipArgument(args);
					if (consumed > 0) {
						args = args.substring(consumed);
					}
				}
			}
		}

		/**
		 * Parses the given String with the first parser that can handle the first
		 * argument on the command-line.
		 *
		 * @param args
		 *            the command line arguments to process
		 */
		@Override
		public void parseArguments(String args, IPath project) {
			parseArguments(null, args, project);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IParserHandler#
		 * getCompilerWorkingDirectory()
		 */
		@Override
		public IPath getCompilerWorkingDirectory() {
			return cwd;
		}

	} // ParserHandler
} // ToolOutputParser