/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.core.internal;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 */
public class NinjaFileReader extends LineNumberReader {

	public NinjaFileReader(Reader reader) {
		super(reader);
	}

	public NinjaFileReader(Reader reader, int sz) {
		super(reader, sz);
	}

	@Override
	public String readLine() throws IOException {
		boolean done = false;
		StringBuilder buffer = new StringBuilder();
		boolean escapedLine = false;
		boolean escapedCommand = false;
		while (!done) {
			String line = super.readLine();
			if (line == null) {
				return null;
			}

			if (escapedLine && line.length() > 0) {
				// Eat the spaces at the beginning.
				int i = 0;
				while (i < line.length() && (Util.isSpace(line.charAt(i)))) {
					i++;
				}
				line = line.substring(i);
			} else if (escapedCommand && line.length() > 0) {
				// Only eat the first tab
				if (line.charAt(0) == '\t') {
					line.substring(1);
				}
			}

			if (Util.isEscapedLine(line)) {
				int index = line.lastIndexOf('$');
				if (index > 0) {
					if (!escapedLine && Util.isCommand(line)) {
						escapedCommand = true;
						buffer.append(line);
					} else {
						escapedLine = true;
						buffer.append(line.substring(0, index));
						buffer.append(' ');
					}
				}
			} else {
				done = true;
				escapedLine = false;
				escapedCommand = false;
				buffer.append(line);
			}
		}
		return buffer.toString();
	}

}
