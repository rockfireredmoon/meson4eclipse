package org.icemoon.meson.cdt.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

public class ArgumentLine {
	private String line;

	public ArgumentLine(String line) {
		this.line = line;
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
	private static List<String> parseQuotedString(String command) {
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
			} else if (c == '"' && !escaped && !singleQuoted) {
				if (quoted) {
					quoted = false;
				} else {
					quoted = true;
				}
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
}
