/*******************************************************************************
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Emerald Icemoon - Initial implementation
 *******************************************************************************/

package org.icemoon.cdt.ninja.core;

import java.util.ArrayList;

/**
 * @author Emerald Icemoon
 */
public class ArgumentLine extends ArrayList<String> {

	private static final long serialVersionUID = 3150854623035058053L;

	public ArgumentLine() {
	}

	public ArgumentLine(String line) {
		parse(line);
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		for (String s : this) {
			if (b.length() > 0)
				b.append(' ');
			if (s.contains(" ")) {
				b.append("\"");
				b.append(s);
				b.append("\"");
			} else
				b.append(s);
		}
		return b.toString();
	}

	public ArgumentLine parse(String command) {
		clear();
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
					add(word.toString());
					word.setLength(0);
					;
				}
			} else {
				word.append(c);
			}
		}
		if (word.length() > 0)
			add(word.toString());
		return this;
	}
}
