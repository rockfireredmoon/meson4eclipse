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
package org.icemoon.cdt.meson.core.ui;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonConsole implements IConsole {

	private static final String CONSOLE_CONTEXT_MENU_ID = "MesonConsole"; //$NON-NLS-1$

	private ConsoleOutputStream os;

	private ConsoleOutputStream es;

	private ConsoleOutputStream is;

	private IConsole console;

	/**
	 */
	public MesonConsole() {
	}

	/**
	 * Starts the console for a given project.
	 *
	 * @param project
	 *            - the project to start the console.
	 */
	@Override
	public void start(IProject project) {
		IBuildConsoleManager fConsoleManager = CUIPlugin.getDefault().getConsoleManager("Meson Console",
				CONSOLE_CONTEXT_MENU_ID);
		console = fConsoleManager.getConsole(project);
		console.start(project);
	}

	/*-
	 * @see org.eclipse.cdt.core.resources.IConsole#getOutputStream()
	 */
	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		if (os == null)
			os = console.getOutputStream();
		return os;
	}

	/*-
	 * @see org.eclipse.cdt.core.resources.IConsole#getInfoStream()
	 */
	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		if (is == null)
			is = console.getInfoStream();
		return is;
	}

	/*-
	 * @see org.eclipse.cdt.core.resources.IConsole#getErrorStream()
	 */
	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		if (es == null)
			es = console.getErrorStream();
		return es;
	}

}
