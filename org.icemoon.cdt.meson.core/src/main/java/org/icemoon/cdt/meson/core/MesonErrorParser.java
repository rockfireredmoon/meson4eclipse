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
package org.icemoon.cdt.meson.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser2;
import org.eclipse.cdt.core.IErrorParser3;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.errorparsers.AbstractErrorParser;
import org.eclipse.cdt.core.errorparsers.ErrorPattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonErrorParser extends AbstractErrorParser implements IErrorParser2, IErrorParser3 {

	public static final String MESON_PROBLEM_MARKER_ID = Activator.PLUGIN_ID + ".problem"; //$NON-NLS-1$

	private static final Pattern PTN_INVOKE_ERROR = Pattern.compile("^Error during basic setup:$");
	private static final Pattern PTN_SCRIPT_ERROR = Pattern
			.compile("^Meson encountered an error in file (.+), line (\\d+), column (\\d+):$");
	private static final Pattern PTN_SCRIPT_ERROR_DESCR1 = Pattern.compile("^Unknown variable \"(.+)\"\\.$");

	public static final String ID = Activator.PLUGIN_ID + ".MesonErrorParser";

	private PState pstate = PState.NONE;
	private ProblemMarkerInfo markerInfo;
	private String fileName;
	private String lineNo;
	private String column;

	public MesonErrorParser() {
		super(new ErrorPattern[] {});
	}

	/**
	 * Overwritten to detect error messages spanning multiple lines.
	 */
	@Override
	public boolean processLine(String line, ErrorParserManager epm) {

		switch (pstate) {
		case NONE:
			Matcher matcher;
			if ((matcher = PTN_SCRIPT_ERROR.matcher(line)).matches()) {
				// "Meson Error at "
				pstate = PState.ERR_SCRIPT;
				fileName = matcher.group(1);
				lineNo = matcher.group(2);
				column = matcher.group(3);
				return true; // consume line
			} else if ((matcher = PTN_INVOKE_ERROR.matcher(line)).matches()) {
				pstate = PState.ERR_INVOKE;
				return true; // consume line
			}
			break;

		case ERR_SCRIPT:
			// marker is present, description is missing
			if (line.length() == 0)
				return true; // consume leading empty line

			pstate = PState.NONE;
			
			// extract problem description
			if ((matcher = PTN_SCRIPT_ERROR_DESCR1.matcher(line)).matches()) {
				createMarker(fileName, epm, lineNo, column, IMarkerGenerator.SEVERITY_ERROR_RESOURCE, matcher.group(1));
				markerInfo.description = line;
			} else {
				createMarker(fileName, epm, lineNo, column, IMarkerGenerator.SEVERITY_ERROR_RESOURCE, null);
				markerInfo.description = line;
			}
			epm.addProblemMarker(markerInfo);
			return true; // consume line

		case ERR_INVOKE:
			if (line.length() > 0) {
				this.markerInfo = new ProblemMarkerInfo(epm.getProject(), -1, line,
						IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
				markerInfo.setType(MESON_PROBLEM_MARKER_ID);
				epm.addProblemMarker(markerInfo);
				pstate = PState.NONE;
			}
			return true; // consume line

		}
		return false;
	}

	/**
	 * Creates a problem marker.
	 *
	 * @param fileName
	 *            the file where the problem has occurred
	 * @param epm
	 * @param lineNo
	 *            the line number of the problem
	 * @param severity
	 *            the severity of the problem, see {@link IMarkerGenerator} for
	 *            acceptable severity values
	 * @param varName
	 *            the name of the variable involved in the error or {@code null} if
	 *            unknown
	 */
	private void createMarker(String fileName, ErrorParserManager epm, String lineNo, String col, int severity,
			String varName) {
		int lineNumber = lineNo == null ? 0 : Integer.parseInt(lineNo);
		int colNumber = col == null ? 0 : Integer.parseInt(col);
		final IProject project = epm.getProject();
		IPath srcPath = project.getLocation();
		IPath filePath = srcPath.append(fileName);
		IFile file2 = project.getFile(filePath.makeRelativeTo(srcPath));
		this.markerInfo = new ProblemMarkerInfo(file2, lineNumber, colNumber, colNumber, null, severity, varName);
		markerInfo.setType(MESON_PROBLEM_MARKER_ID);
	}

	/*-
	 * @see org.eclipse.cdt.core.IErrorParser2#getProcessLineBehaviour()
	 */
	@Override
	public int getProcessLineBehaviour() {
		return IErrorParser2.KEEP_LONGLINES;
	}

	////////////////////////////////////////////////////////////////////
	// inner classes
	////////////////////////////////////////////////////////////////////
	/**
	 * Error parser state for multiline errors
	 */
	private enum PState {
		/** no meson error detected. */
		NONE,
		/** meson invocation failed (no process) */
		ERR_INVOKE,
		/** error in MesonLists.txt */
		ERR_SCRIPT,
	}

	/*-
	 * @see org.eclipse.cdt.core.IErrorParser3#shutdown()
	 */
	@Override
	public void shutdown() {
		pstate = PState.NONE;
		markerInfo = null;
	}
}
