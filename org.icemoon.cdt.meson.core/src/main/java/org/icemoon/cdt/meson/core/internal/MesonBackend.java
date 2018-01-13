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
package org.icemoon.cdt.meson.core.internal;

/**
 * Represents a meson buildscript generator including information about the
 * makefile (buildscript) processor that builds from the generated script.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public enum MesonBackend {
	/*
	 * Implementation Note: Please not include generators for IDE project files,
	 * such as "Eclipse CDT4 - Unix Makefiles".
	 */

	XCODE("XCode", "xcode"), 
	VS("Visual Studio", "vs"), 
	VS2010("Visual Studio 2010", "vs2010"), 
	VS2015("Visual Studio 2015", "vs2015"), 
	VS2017("Visual Studio 2017", "vs2017"), 
	NINJA("Ninja", "ninja", "-k 0") {
		@Override
		public String getMakefileName() {
			return "build.ninja";
		}
	};

	private final String name;
	private final String buildscriptProcessorCommand;
	private String ignoreErrOption;

	private MesonBackend(String name, String buildscriptProcessorCommand, String ignoreErrOption) {
		this.name = name;
		this.buildscriptProcessorCommand = buildscriptProcessorCommand;
		this.ignoreErrOption = ignoreErrOption;
	}

	private MesonBackend(String name, String buildscriptProcessorCommand) {
		this(name, buildscriptProcessorCommand, "-k");
	}

	private MesonBackend(String name) {
		this(name, "make");
	}

	/**
	 * Gets the backend name that specifies the buildscript generator.
	 *
	 * @return a non-empty string, which must be a valid argument for meson's -G
	 *         option.
	 */
	public String getBackendName() {
		return name;
	}

	/**
	 * Gets the name of the makefile (buildscript) processor.
	 */
	public String getBuildscriptProcessorCommand() {
		return buildscriptProcessorCommand;
	}

	/**
	 * Gets the name of the top-level makefile (buildscript) which is interpreted by
	 * the buildscript processor.
	 *
	 * @return name of the makefile.
	 */
	public String getMakefileName() {
		return "Makefile";
	}

	/**
	 * Gets the extra argument to pass to the buildscript processor.
	 *
	 * @return a non-empty string, or {@code null} if no extra argument should be
	 *         passed.
	 */
	public String getBuildscriptProcessorExtraArg() {
		return null;
	}

	/**
	 * Gets the buildscript processor´s command option to ignore build errors.
	 *
	 * @return the command option string or {@code null} if no option is needed.
	 */
	public String getIgnoreErrOption() {
		return ignoreErrOption;
	}
}
