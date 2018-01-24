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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Various tool argument parser implementations.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
class ToolArgumentParsers {

	/** matches a macro name, with optional macro argument list */
	private static final String REGEX_MACRO_NAME = "([\\w$]+)(?:\\(([\\w$, ]+)\\))?";
	/**
	 * matches a macro name, skipping leading whitespace. Name in matcher group 1
	 */
	private static final String REGEX_MACRO_NAME_SKIP_LEADING_WS = "\\s*" + REGEX_MACRO_NAME;
	/**
	 * matches a macro name with quotes, skipping leading whitespace. Name in
	 * matcher group 2
	 */
	private static final String REGEX_MACRO_NAME_QUOTED__SKIP_LEADING_WS = "\\s*([\"'])" + REGEX_MACRO_NAME;
	/** matches an include path with quoted directory. Name in matcher group 2 */
	private static final String REGEX_INCLUDEPATH_QUOTED_DIR = "\\s*([\"'])(.+?)\\1";
	/**
	 * matches an include path with unquoted directory. Name in matcher group 1
	 */
	private static final String REGEX_INCLUDEPATH_UNQUOTED_DIR = "\\s*([^\\s]+)";

	/**
	 * nothing to instantiate
	 */
	private ToolArgumentParsers() {
	}

	/**
	 * Parse a space separated string into a list, treating portions quotes with
	 * single quotes as a single element. Single quotes themselves and spaces can be
	 * escaped with a backslash.
	 * 
	 * @param command
	 *            command to parse
	 * @return parsed command
	 */
	public static List<String> parseQuotedString(String command) {
		List<String> args = new ArrayList<String>();
		boolean escaped = false;
		boolean quoted = false;
		boolean singleQuoted = false;
		StringBuilder word = new StringBuilder();
		for (int i = 0; i < command.length(); i++) {
			char c = command.charAt(i);
			if (c == '\'' && !escaped && !quoted) {
				if (singleQuoted)
					singleQuoted = false;
				else
					singleQuoted = true;
				word.append(c);
			} else if (c == '"' && !escaped && !singleQuoted) {
				if (quoted) {
					quoted = false;
				} else {
					quoted = true;
				}
				word.append(c);
			} else if (c == '\\' && !escaped) {
				escaped = true;
			} else if (c == ' ' && !escaped && !quoted && !singleQuoted) {
				if (word.length() > 0) {
					args.add(word.toString());
					word.setLength(0);
					;
				}
			} else {
				word.append(c);
			}
		}
		if (word.length() > 0)
			args.add(word.toString());
		return args;
	}

	////////////////////////////////////////////////////////////////////
	// Matchers for options
	////////////////////////////////////////////////////////////////////
	/**
	 * A matcher for option names. Includes information of the matcher groups that
	 * hold the option name.
	 *
	 * @author Martin Weber @author Emerald Icemoon
	 */
	static class NameOptionMatcher {
		final Matcher matcher;
		final int nameGroup;

		/**
		 * Constructor.
		 *
		 * @param pattern
		 *            - regular expression pattern being parsed by the parser.
		 * @param nameGroup
		 *            - capturing group number defining name of an entry.
		 */
		public NameOptionMatcher(String pattern, int nameGroup) {
			this.matcher = Pattern.compile(pattern).matcher("");
			this.nameGroup = nameGroup;
		}

		@Override
		public String toString() {
			return "NameOptionMatcher [matcher=" + this.matcher + ", nameGroup=" + this.nameGroup + "]";
		}
	}

	/**
	 * A matcher for preprocessor define options. Includes information of the
	 * matcher groups that hold the macro name and value.
	 *
	 * @author Martin Weber @author Emerald Icemoon
	 */
	static class NameValueOptionMatcher extends NameOptionMatcher {
		private final int valueGroup;

		/**
		 * Constructor.
		 *
		 * @param pattern
		 *            - regular expression pattern being parsed by the parser.
		 * @param nameGroup
		 *            - capturing group number defining name of an entry.
		 * @param valueGroup
		 *            - capturing group number defining value of an entry.
		 */
		/**
		 * @param pattern
		 * @param nameGroup
		 * @param valueGroup
		 */
		public NameValueOptionMatcher(String pattern, int nameGroup, int valueGroup) {
			super(pattern, nameGroup);
			this.valueGroup = valueGroup;
		}

		@Override
		public String toString() {
			return "NameValueOptionMatcher [matcher=" + this.matcher + ", nameGroup=" + this.nameGroup + ", valueGroup="
					+ this.valueGroup + "]";
		}
	}

	////////////////////////////////////////////////////////////////////
	// generic option parsers
	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a C-compiler macro definition
	 * argument.
	 */
	private static abstract class MacroDefineGeneric {

		protected final int processArgument(List<ICLanguageSettingEntry> returnedEntries, String args,
				NameValueOptionMatcher[] optionMatchers) {
			for (NameValueOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(args);
				if (matcher.lookingAt()) {
					final String name = matcher.group(oMatcher.nameGroup);
					final String value = matcher.group(oMatcher.valueGroup);
					final ICLanguageSettingEntry entry = CDataUtil.createCMacroEntry(name, value,
							ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
					returnedEntries.add(entry);
					final int end = matcher.end();
					return end;
				}
			}
			return 0;// no input consumed
		}
	}

	/**
	 * A tool argument parser capable to parse a C-compiler macro cancel argument.
	 */
	private static class MacroUndefineGeneric {

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgument(java.util.List, java.lang.String)
		 */
		protected final int processArgument(List<ICLanguageSettingEntry> returnedEntries, String argsLine,
				NameOptionMatcher optionMatcher) {
			final Matcher oMatcher = optionMatcher.matcher;

			oMatcher.reset(argsLine);
			if (oMatcher.lookingAt()) {
				final String name = oMatcher.group(1);
				final ICLanguageSettingEntry entry = CDataUtil.createCMacroEntry(name, null,
						ICSettingEntry.UNDEFINED | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
				returnedEntries.add(entry);
				final int end = oMatcher.end();
				return end;
			}
			return 0;// no input consumed
		}
	}

	/**
	 * A tool argument parser capable to parse a C-compiler include path argument.
	 */
	private static abstract class IncludePathGeneric {
		/**
		 * @param cwd
		 *            the current working directory of the compiler at its invocation
		 * @see org.icemoon.cdt.ninja.core.language.settings.providers.IToolArgumentParser#processArgument(List,
		 *      IPath, String, IPath)
		 */
		protected final int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				NameOptionMatcher[] optionMatchers, IPath project) {
			for (NameOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(argsLine);
				if (matcher.lookingAt()) {
					String name = matcher.group(oMatcher.nameGroup);
					IPath path = Path.fromOSString(name);
					if (!path.isAbsolute()) {
						path = cwd.append(path);
					}
					ICLanguageSettingEntry entry;
					if (project.isPrefixOf(path)) {
						path = path.makeRelativeTo(project);
						entry = CDataUtil.createCIncludePathEntry("/" + project.lastSegment() + "/" + path.toString(),
								ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
					} else {
						entry = CDataUtil.createCIncludePathEntry(path.toOSString(), ICSettingEntry.RESOLVED);
					}
					returnedEntries.add(entry);
					final int end = matcher.end();
					return end;
				}
			}
			return 0;// no input consumed
		}
	}

	/**
	 * A tool argument parser capable to parse a C-compiler include path argument.
	 */
	private static abstract class LibraryPathGeneric {
		/**
		 * @param cwd
		 *            the current working directory of the compiler at its invocation
		 * @see org.icemoon.cdt.ninja.core.language.settings.providers.IToolArgumentParser#processArgument(List,
		 *      IPath, String, IPath)
		 */
		protected final int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				NameOptionMatcher[] optionMatchers, IPath project) {
			for (NameOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(argsLine);
				if (matcher.lookingAt()) {
					String name = matcher.group(oMatcher.nameGroup);
					ICLanguageSettingEntry entry;
					entry = CDataUtil.createCLibraryFileEntry(name, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
					returnedEntries.add(entry);
					final int end = matcher.end();
					return end;
				}
			}
			return 0;// no input consumed
		}
	}

	////////////////////////////////////////////////////////////////////
	// POSIX compatible option parsers
	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a POSIX compatible C-compiler macro
	 * definition argument: {@code -DNAME=value}.
	 */
	static class MacroDefine_C_POSIX extends MacroDefineGeneric implements IToolArgumentParser {

		private static final NameValueOptionMatcher[] optionMatchers = {
				/* quoted value, whitespace in value, w/ macro arglist */
				new NameValueOptionMatcher("-D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "((?:=)([\"'])(.+?)\\4)", 1, 5),
				/* w/ macro arglist */
				new NameValueOptionMatcher("-D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "((?:=)(\\S+))?", 1, 4),
				/* quoted name, whitespace in value, w/ macro arglist */
				new NameValueOptionMatcher("-D" + REGEX_MACRO_NAME_QUOTED__SKIP_LEADING_WS + "((?:=)(.+?))?\\1", 2, 5),
				/* w/ macro arglist, shell escapes \' and \" in value */
				new NameValueOptionMatcher("-D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "(?:=)((\\\\([\"']))(.*?)\\2)", 1,
						2) };

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				IPath project) {
			List<String> args = parseQuotedString(argsLine);
			if (!args.isEmpty()) {
				String firstArg = args.get(0);
				String procArg = firstArg;
				if (firstArg.startsWith("'")) {
					procArg = firstArg.substring(1, firstArg.length() - 1);
				}
				int consumed = processArgument(returnedEntries, procArg, optionMatchers);
				if (consumed > 0 && firstArg.startsWith("'")) {
					return firstArg.length();
				} else
					return consumed;
			}
			return 0;
		}

	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a POSIX compatible C-compiler macro
	 * cancel argument: {@code -UNAME}.
	 */
	static class MacroUndefine_C_POSIX extends MacroUndefineGeneric implements IToolArgumentParser {

		private static final NameOptionMatcher optionMatcher = new NameOptionMatcher(
				"-U" + REGEX_MACRO_NAME_SKIP_LEADING_WS, 1);

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgument(java.util.List, java.lang.String)
		 */
		@Override
		public int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				IPath project) {
			List<String> args = parseQuotedString(argsLine);
			if (!args.isEmpty()) {
				String firstArg = args.get(0);
				String procArg = firstArg;
				if (firstArg.startsWith("'")) {
					procArg = firstArg.substring(1, firstArg.length() - 1);
				}
				int consumed = processArgument(returnedEntries, procArg, optionMatcher);
				if (consumed > 0 && firstArg.startsWith("'")) {
					return firstArg.length();
				} else
					return consumed;
			}
			return 0;
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a POSIX compatible C-link path
	 * argument: {@code -llibrary}.
	 */
	static class LibraryPath_C_POSIX extends LibraryPathGeneric implements IToolArgumentParser {
		private static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("-l" + REGEX_INCLUDEPATH_QUOTED_DIR, 2),
				/* unquoted directory */
				new NameOptionMatcher("-l" + REGEX_INCLUDEPATH_UNQUOTED_DIR, 1) };

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				IPath project) {

			List<String> args = parseQuotedString(argsLine);
			if (!args.isEmpty()) {
				String firstArg = args.get(0);
				String procArg = firstArg;
				if (firstArg.startsWith("'")) {
					procArg = firstArg.substring(1, firstArg.length() - 1);
				}
				int consumed = processArgument(returnedEntries, cwd, procArg, optionMatchers, project);
				if (consumed > 0 && firstArg.startsWith("'")) {
					return firstArg.length();
				} else
					return consumed;
			}
			return 0;
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a POSIX compatible C-compiler include
	 * path argument: {@code -Ipath}.
	 */
	static class IncludePath_C_POSIX extends IncludePathGeneric implements IToolArgumentParser {
		private static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("-I" + REGEX_INCLUDEPATH_QUOTED_DIR, 2),
				/* unquoted directory */
				new NameOptionMatcher("-I" + REGEX_INCLUDEPATH_UNQUOTED_DIR, 1) };

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				IPath project) {

			List<String> args = parseQuotedString(argsLine);
			if (!args.isEmpty()) {
				String firstArg = args.get(0);
				String procArg = firstArg;
				if (firstArg.startsWith("'")) {
					procArg = firstArg.substring(1, firstArg.length() - 1);
				}
				int consumed = processArgument(returnedEntries, cwd, procArg, optionMatchers, project);
				if (consumed > 0 && firstArg.startsWith("'")) {
					return firstArg.length();
				} else
					return consumed;
			}
			return 0;
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a C-compiler system include path
	 * argument: {@code -system path}.
	 */
	static class SystemIncludePath_C implements IToolArgumentParser {
		static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("-isystem" + "\\s+([\"'])(.+?)\\1", 2),
				/* unquoted directory */
				new NameOptionMatcher("-isystem" + "\\s+([^\\s]+)", 1), };

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				IPath project) {
			return processArgument(returnedEntries, argsLine, optionMatchers);
		}

		protected final int processArgument(List<ICLanguageSettingEntry> returnedEntries, String argsLine,
				NameOptionMatcher[] optionMatchers) {
			for (NameOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(argsLine);
				if (matcher.lookingAt()) {
					final String name = matcher.group(oMatcher.nameGroup);
					final ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(name,
							ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
					returnedEntries.add(entry);
					final int end = matcher.end();
					return end;
				}
			}
			return 0;// no input consumed
		}
	}

	////////////////////////////////////////////////////////////////////
	// POSIX compatible option parsers
	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a cl (Microsoft c compiler)
	 * compatible C-compiler macro definition argument: {@code /DNAME=value}.
	 */
	static class MacroDefine_C_CL extends MacroDefineGeneric implements IToolArgumentParser {

		private static final NameValueOptionMatcher[] optionMatchers = {
				/* quoted value, whitespace in value, w/ macro arglist */
				new NameValueOptionMatcher("/D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "((?:=)([\"'])(.+?)\\4)", 1, 5),
				/* w/ macro arglist */
				new NameValueOptionMatcher("/D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "((?:=)(\\S+))?", 1, 4),
				/* quoted name, whitespace in value, w/ macro arglist */
				new NameValueOptionMatcher("/D" + REGEX_MACRO_NAME_QUOTED__SKIP_LEADING_WS + "((?:=)(.+?))?\\1", 2, 5),
				/* w/ macro arglist, shell escapes \' and \" in value */
				new NameValueOptionMatcher("/D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "(?:=)((\\\\([\"']))(.*?)\\2)", 1,
						2), };

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				IPath project) {
			return processArgument(returnedEntries, argsLine, optionMatchers);
		}

	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a cl (Microsoft c compiler)
	 * compatible C-compiler macro cancel argument: {@code /UNAME}.
	 */
	static class MacroUndefine_C_CL extends MacroUndefineGeneric implements IToolArgumentParser {

		private static final NameOptionMatcher optionMatcher = new NameOptionMatcher(
				"/U" + REGEX_MACRO_NAME_SKIP_LEADING_WS, 1);

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgument(java.util.List, java.lang.String)
		 */
		@Override
		public int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				IPath project) {
			return processArgument(returnedEntries, argsLine, optionMatcher);
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a cl (Microsoft c compiler)
	 * compatible C-compiler include path argument: {@code /Ipath}.
	 */
	static class IncludePath_C_CL extends IncludePathGeneric implements IToolArgumentParser {
		private static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("/I" + REGEX_INCLUDEPATH_QUOTED_DIR, 2),
				/* unquoted directory */
				new NameOptionMatcher("/I" + REGEX_INCLUDEPATH_UNQUOTED_DIR, 1), };

		/*-
		 * @see org.icemoon.cdt.meson.ui.language.settings.providers.IToolArgumentParser#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(List<ICLanguageSettingEntry> returnedEntries, IPath cwd, String argsLine,
				IPath project) {
			return processArgument(returnedEntries, cwd, argsLine, optionMatchers, project);
		}
	}

	////////////////////////////////////////////////////////////////////
}
