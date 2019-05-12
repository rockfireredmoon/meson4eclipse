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
package org.icemoon.cdt.meson.core.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.ProjectOption;
import org.icemoon.cdt.meson.core.settings.internal.ProjectOptionSerializer;
import org.icemoon.cdt.meson.core.settings.internal.Util;

/**
 * Holds per-configuration project settings.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonPreferences {

	/**
	 * storage ID used to store settings or preferences with a
	 * ICConfigurationDescription
	 */
	public static final String CFG_STORAGE_ID = Activator.PLUGIN_ID + ".settings";
	private static final String ATTR_WARN_LEVEL = "warnLevel";
	private static final String ATTR_STRIP = "strip";
	private static final String ATTR_STD_SPLIT = "stdSplit";
	private static final String ATTR_WERROR = "werror";
	private static final String ATTR_BUILD_TYPE = "buildType";
	private static final String ATTR_LAYOUT = "layout";
	private static final String ATTR_UNITY = "unity";
	private static final String ATTR_ERROR_LOGS = "errorLogs";
	private static final String ATTR_DEFAULT_LIBRARY = "defaultLibrary";
	/**  */
	static final String ELEM_PROJECT_OPTIONS = "projectOptions";
	/**  */
	private static final String ELEM_OPTIONS = "options";
	private static final String ATTR_BUILD_DIR = "buildDir";
	private static final String ATTR_SERIAL = "serial";
	private static final String ATTR_STAMP = "stamp";

	private boolean strip, stdSplit, werror;
	private int warnLevel;

	private List<ProjectOption> projectOptions = new ArrayList<>(0);
	private String buildDirectory;
	private String crossFile;
	private MesonBuildType buildType;
	private MesonDefaultLibrary defaultLibrary;
	private MesonLayout layout;
	private LinuxPreferences linuxPreferences = new LinuxPreferences();
	private WindowsPreferences windowsPreferences = new WindowsPreferences();
	private MacOSXPreferences macOSXPreferences = new MacOSXPreferences();
	private MesonUnity unity;
	private boolean errorLogs;
	private UUID serial;
	private UUID stamp;

	/**
	 * Creates a new object, initialized with all default values.
	 */
	public MesonPreferences() {
		reset();
	}

	/**
	 * Sets each value to its default.
	 */
	public void reset() {
		warnLevel = 1;
		strip = false;
		stdSplit = false;
		werror = false;
		projectOptions.clear();
		crossFile = null;
		defaultLibrary = MesonDefaultLibrary.SHARED;
		buildType = null;
		layout = MesonLayout.MIRROR;
		unity = MesonUnity.OFF;
		errorLogs = true;
		serial = null;
		stamp = null;
		// linuxPreferences.reset();
		// windowsPreferences.reset();
	}
	
	public boolean isNeedsReconfigure() {
		return serial == null || !Objects.equals(serial, stamp);
	}
	
	public void needsReconfigure() {
		stamp = UUID.randomUUID();
	}
	
	public void reconfigured() {
		serial = stamp;
	}

	/**
	 * Initializes the configuration information from the storage element specified
	 * in the argument.
	 *
	 * @param parent
	 *            A storage element containing the configuration information. If
	 *            {@code null}, nothing is loaded from storage.
	 */
	public void loadFromStorage(ICStorageElement parent) {
		if (parent == null)
			return;

		final ICStorageElement[] children = parent.getChildren();
		for (ICStorageElement child : children) {
			if (ELEM_OPTIONS.equals(child.getName())) {
				// options...
				try {
					warnLevel = Integer.parseInt(child.getAttribute(ATTR_WARN_LEVEL));
				} catch (NumberFormatException nfe) {
					warnLevel = 1;
				}
				try {
					buildType = MesonBuildType.valueOf(child.getAttribute(ATTR_BUILD_TYPE));
				} catch (Exception e) {
					buildType = MesonBuildType.DEBUG;
				}
				try {
					layout = MesonLayout.valueOf(child.getAttribute(ATTR_LAYOUT));
				} catch (Exception e) {
					layout = MesonLayout.MIRROR;
				}
				try {
					defaultLibrary = MesonDefaultLibrary.valueOf(child.getAttribute(ATTR_DEFAULT_LIBRARY));
				} catch (Exception e) {
					defaultLibrary = MesonDefaultLibrary.SHARED;
				}
				try {
					unity = MesonUnity.valueOf(child.getAttribute(ATTR_UNITY));
				} catch (Exception e) {
					unity = MesonUnity.OFF;
				}
				errorLogs = Boolean.parseBoolean(child.getAttribute(ATTR_ERROR_LOGS));
				strip = Boolean.parseBoolean(child.getAttribute(ATTR_STRIP));
				stdSplit = Boolean.parseBoolean(child.getAttribute(ATTR_STD_SPLIT));
				werror = Boolean.parseBoolean(child.getAttribute(ATTR_WERROR));
				buildDirectory = parent.getAttribute(ATTR_BUILD_DIR);
				serial =  parent.getAttribute(ATTR_SERIAL) == null ? null : UUID.fromString(parent.getAttribute(ATTR_SERIAL));
				stamp =  parent.getAttribute(ATTR_STAMP) == null ? null : UUID.fromString(parent.getAttribute(ATTR_STAMP));
			} else if (ELEM_PROJECT_OPTIONS.equals(child.getName())) {
				// projectOptions...
				Util.deserializeCollection(projectOptions, new ProjectOptionSerializer(), child);
			}
		}
		linuxPreferences.loadFromStorage(parent);
		windowsPreferences.loadFromStorage(parent);
		macOSXPreferences.loadFromStorage(parent);
	}

	/**
	 * Persists this configuration to the project file.
	 */
	public void saveToStorage(ICStorageElement parent) {
		ICStorageElement pOpts;
		ICStorageElement[] options = parent.getChildrenByName(ELEM_OPTIONS);
		if (options.length > 0) {
			pOpts = options[0];
		} else {
			pOpts = parent.createChild(ELEM_OPTIONS);
		}
		if (warnLevel != 1) {
			pOpts.setAttribute(ATTR_WARN_LEVEL, String.valueOf(warnLevel));
		} else {
			pOpts.removeAttribute(ATTR_WARN_LEVEL);
		}
		if (buildType != null) {
			pOpts.setAttribute(ATTR_BUILD_TYPE, buildType.name());
		} else {
			pOpts.removeAttribute(ATTR_BUILD_TYPE);
		}
		if (unity != null) {
			pOpts.setAttribute(ATTR_UNITY, unity.name());
		} else {
			pOpts.removeAttribute(ATTR_UNITY);
		}
		if (layout != null) {
			pOpts.setAttribute(ATTR_LAYOUT, layout.name());
		} else {
			pOpts.removeAttribute(ATTR_LAYOUT);
		}
		if (layout != null) {
			pOpts.setAttribute(ATTR_DEFAULT_LIBRARY, defaultLibrary.name());
		} else {
			pOpts.removeAttribute(ATTR_DEFAULT_LIBRARY);
		}
		if (strip) {
			pOpts.setAttribute(ATTR_STRIP, String.valueOf(strip));
		} else {
			pOpts.removeAttribute(ATTR_STRIP);
		}
		if (errorLogs) {
			pOpts.setAttribute(ATTR_ERROR_LOGS, String.valueOf(errorLogs));
		} else {
			pOpts.removeAttribute(ATTR_ERROR_LOGS);
		}
		if (stdSplit) {
			pOpts.setAttribute(ATTR_STD_SPLIT, String.valueOf(stdSplit));
		} else {
			pOpts.removeAttribute(ATTR_STD_SPLIT);
		}
		if (werror) {
			pOpts.setAttribute(ATTR_WERROR, String.valueOf(werror));
		} else {
			pOpts.removeAttribute(ATTR_WERROR);
		}
		if (buildDirectory != null) {
			parent.setAttribute(ATTR_BUILD_DIR, buildDirectory);
		} else {
			parent.removeAttribute(ATTR_BUILD_DIR);
		}
		if (serial != null) {
			parent.setAttribute(ATTR_SERIAL, serial.toString());
		} else {
			parent.removeAttribute(ATTR_SERIAL);
		}
		if (stamp != null) {
			parent.setAttribute(ATTR_STAMP, stamp.toString());
		} else {
			parent.removeAttribute(ATTR_STAMP);
		}

		// projectOptions...
		Util.serializeCollection(ELEM_PROJECT_OPTIONS, parent, new ProjectOptionSerializer(), projectOptions);
		// linuxPreferences.saveToStorage(parent);
		// windowsPreferences.saveToStorage(parent);
	}

	/**
	 * {@code --layout}
	 */
	public MesonLayout getLayout() {
		return layout;
	}

	/**
	 * {@code --layout}
	 */
	public void setLayout(MesonLayout layout) {
		this.layout = layout;
	}

	/**
	 * {@code --layout}
	 */
	public MesonDefaultLibrary getDefaultLibrary() {
		return defaultLibrary;
	}

	/**
	 * {@code --default-library}
	 */
	public void setDefaultLibrary(MesonDefaultLibrary defaultLibrary) {
		this.defaultLibrary = defaultLibrary;
	}

	/**
	 * {@code --warnlevel}
	 */
	public int getWarnLevel() {
		return warnLevel;
	}

	/**
	 * {@code --strip}
	 */
	public boolean isStrip() {
		return strip;
	}

	/**
	 * {@code --strip}
	 */
	public void setStrip(boolean strip) {
		this.strip = strip;
	}

	/**
	 * {@code --stdsplit}
	 */
	public boolean isStdSplit() {
		return stdSplit;
	}

	/**
	 * {@code --stdsplit}
	 */
	public void setStdSplit(boolean stdSplit) {
		this.stdSplit = stdSplit;
	}

	/**
	 * {@code --unity}
	 */
	public MesonUnity getUnity() {
		return unity;
	}

	/**
	 * {@code --unity}
	 */
	public void setUnity(MesonUnity unity) {
		this.unity = unity;
	}

	/**
	 * {@code --errorlogs}
	 */
	public boolean isErrorLogs() {
		return errorLogs;
	}

	/**
	 * {@code --errorlogs}
	 */
	public void setErrorLogs(boolean errorLogs) {
		this.errorLogs = errorLogs;
	}

	/**
	 * {@code --werror}
	 */
	public boolean isWarningsAsErrors() {
		return werror;
	}

	/**
	 * {@code --werror}
	 */
	public void setWarningsAsErrors(boolean werror) {
		this.werror = werror;
	}

	/**
	 * {@code --warnlevel}
	 */
	public void setWarnLevel(int warnLevel) {
		this.warnLevel = warnLevel;
	}

	/**
	 * {@code --buildtype}
	 */
	public void setBuildType(MesonBuildType buildType) {
		this.buildType = buildType;
	}

	/**
	 * {@code --buildtype}
	 */
	public MesonBuildType getBuildType() {
		return buildType;
	}

	/**
	 * Gets the list of meson variable to define on the meson command-line.
	 *
	 * @return a mutable list, never {@code null}
	 */
	public List<ProjectOption> getProjectOptions() {
		return projectOptions;
	}

	public LinuxPreferences getLinuxPreferences() {
		return linuxPreferences;
	}

	public WindowsPreferences getWindowsPreferences() {
		return windowsPreferences;
	}

	public MacOSXPreferences getMacOSXPreferences() {
		return macOSXPreferences;
	}

	/**
	 * Gets the name of the file that is used for describing cross compilation
	 * environment. {@code --cross-file}
	 *
	 * @return the file name or <code>null</code> if the there is no cross
	 *         compilation.
	 */
	public String getCrossFile() {
		return crossFile;
	}

	/**
	 * Sets the name of the file that is used for describing cross compilation
	 * environment. {@code --cross-file}
	 *
	 * @param crossFile
	 *            the file name or <code>null</code> if there is no cross
	 *            compilation.
	 */
	public void setCrossFile(String crossFile) {
		this.crossFile = crossFile;
	}

	/**
	 * Gets the name of the build directory.
	 *
	 * @return the build directory name or <code>null</code> if the hard-coded
	 *         default shall be used.
	 */
	public String getBuildDirectory() {
		return buildDirectory;
	}

	/**
	 * Sets the name of the build directory.
	 *
	 * @param buildDirectory
	 *            the build directory name or <code>null</code> if the hard-coded
	 *            default shall be used.
	 */
	public void setBuildDirectory(String buildDirectory) {
		this.buildDirectory = buildDirectory;
	}

}