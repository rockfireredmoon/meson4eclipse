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

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.runtime.Platform;
import org.icemoon.cdt.meson.core.MesonBackend;
import org.icemoon.cdt.meson.core.ProjectOption;
import org.icemoon.cdt.meson.core.settings.internal.ProjectOptionSerializer;
import org.icemoon.cdt.meson.core.settings.internal.Util;

/**
 * Preferences that override/augment the generic properties when running under a
 * specific OS.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public abstract class AbstractOsPreferences {
	private static final String ATTR_COMMAND = "command";
	private static final String ATTR_USE_DEFAULT_COMMAND = "use-default";
	private static final String ATTR_GENERATOR = "generator";
	private static final String ATTR_BUILD_COMMAND = "build_command";

	private String command;
	private MesonBackend generator;
	private String buildscriptProcessorCmd;
	private boolean useDefaultCommand;
	private List<ProjectOption> projectOptions = new ArrayList<ProjectOption>(0);
	private MesonBackend generatedWith;

	/**
	 * Creates a new object, initialized with all default values.
	 */
	public AbstractOsPreferences() {
		reset();
		// after startup: assume the makefiles have been generated with the saved
		// generator
		generatedWith = generator;
	}

	/**
	 * Gets the name of the storage element that store this preferences.
	 */
	protected abstract String getStorageElementName();

	/**
	 * Sets each value to its default.
	 */
	public void reset() {
		useDefaultCommand = true;
		setCommand("meson");
		setBackend(MesonBackend.XCODE);
		setBuildscriptProcessorCommand(null);
		projectOptions.clear();
	}

	/**
	 * Gets the platform specific preferences object from the specified preferences
	 * for the current operating system (the OS we are running under). If the OS
	 * cannot be determined or its specific preferences are not implemented, the
	 * platform specific preferences for Linux are returned as a fall-back.
	 *
	 * @return the platform specific or fall-back preferences
	 */
	public static AbstractOsPreferences extractOsPreferences(MesonPreferences prefs) {
		final String os = Platform.getOS();
		if (Platform.OS_WIN32.equals(os)) {
			return prefs.getWindowsPreferences();
		}
		else if (Platform.OS_MACOSX.equals(os)) {
			return prefs.getMacOSXPreferences();
		} else {
			// fall back to linux, if OS is unknown
			return prefs.getLinuxPreferences();
		}
	}

	/**
	 * Gets whether to use the default meson command.
	 */
	public final boolean getUseDefaultCommand() {
		return useDefaultCommand;
	}

	/**
	 * Sets whether to use the default meson command.
	 */
	public void setUseDefaultCommand(boolean useDefaultCommand) {
		this.useDefaultCommand = useDefaultCommand;
	}

	/**
	 * Gets the meson command.
	 */
	public final String getCommand() {
		return command;
	}

	/**
	 * Sets the meson command.
	 */
	public void setCommand(String command) {
		if (command == null) {
			throw new NullPointerException("command");
		}
		this.command = command;
	}

	/**
	 * Gets the meson buildscript backend.
	 */
	public final MesonBackend getBackend() {
		return generator;
	}

	/**
	 * Sets the meson build-script backend.
	 */
	public void setBackend(MesonBackend backend) {
		if (backend == null) {
			throw new NullPointerException("backend");
		}
		this.generator = backend;
	}

	/**
	 * Gets the buildscript processor name.
	 *
	 * @return the buildscript processor or {@code null} if the build command
	 *         matching the chosen generator should be used.
	 */
	public String getBuildscriptProcessorCommand() {
		return buildscriptProcessorCmd;
	}

	/**
	 * Sets the buildscript processor name.
	 *
	 * @param buildscriptProcessorCommand
	 *            the buildscript processor. If {@code null} or an empty string, the
	 *            build command matching the chosen generator should be used.
	 */
	public void setBuildscriptProcessorCommand(String buildscriptProcessorCommand) {
		if ("".equals(buildscriptProcessorCommand))
			buildscriptProcessorCommand = null;
		this.buildscriptProcessorCmd = buildscriptProcessorCommand;
	}

	/**
	 * Gets the list of meson variable to define on the meson command-line.
	 *
	 * @return a mutable list, never {@code null}
	 */
	public final List<ProjectOption> getProjectOptions() {
		return projectOptions;
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
		// loop to merge multiple children of the same name
		final ICStorageElement[] children = parent.getChildrenByName(getStorageElementName());
		for (ICStorageElement child : children) {
			loadChildFromStorage(child);
		}
	}

	private void loadChildFromStorage(ICStorageElement parent) {
		String val;
		// use default command
		val = parent.getAttribute(ATTR_USE_DEFAULT_COMMAND);
		useDefaultCommand = Boolean.parseBoolean(val);
		// command
		val = parent.getAttribute(ATTR_COMMAND);
		if (val != null)
			setCommand(val);
		// generator
		val = parent.getAttribute(ATTR_GENERATOR);
		if (val != null) {
			try {
				setBackend(MesonBackend.valueOf(val));
			} catch (IllegalArgumentException ex) {
				// fall back to default generator
			}
		}
		val = parent.getAttribute(ATTR_BUILD_COMMAND);
		// if (val != null)
		setBuildscriptProcessorCommand(val);

		ICStorageElement[] children = parent.getChildren();
		for (ICStorageElement child : children) {
			if (MesonPreferences.ELEM_PROJECT_OPTIONS.equals(child.getName())) {
				// projectOptions...
				Util.deserializeCollection(projectOptions, new ProjectOptionSerializer(), child);
			}
		}
	}

	/**
	 * Persists this configuration to the project file.
	 */
	public void saveToStorage(ICStorageElement parent) {
		final String storageElementName = getStorageElementName();

		final ICStorageElement[] children = parent.getChildrenByName(storageElementName);
		if (children.length == 0) {
			parent = parent.createChild(storageElementName);
		} else {
			// take first child
			parent = children[0];
		}

		// use default command
		if (useDefaultCommand) {
			parent.setAttribute(ATTR_USE_DEFAULT_COMMAND, String.valueOf(useDefaultCommand));
		} else {
			parent.removeAttribute(ATTR_USE_DEFAULT_COMMAND);
		}
		parent.setAttribute(ATTR_COMMAND, command);
		// generator
		parent.setAttribute(ATTR_GENERATOR, generator.name());
		if (buildscriptProcessorCmd != null) {
			parent.setAttribute(ATTR_BUILD_COMMAND, buildscriptProcessorCmd);
		} else {
			parent.removeAttribute(ATTR_BUILD_COMMAND);
		}
		// projectOptions...
		Util.serializeCollection(MesonPreferences.ELEM_PROJECT_OPTIONS, parent, new ProjectOptionSerializer(),
				projectOptions);
	}

	/**
	 * Gets the generator that was used to generate the Meson cache file and the
	 * makefiles. This property is not persisted.
	 */
	public MesonBackend getGeneratedWith() {
		return this.generatedWith;
	}

	/**
	 * Sets the generator that was used to generate the Meson cache file and the
	 * makefiles.
	 */
	public void setGeneratedWith(MesonBackend generator) {
		this.generatedWith = generator;
	}

}