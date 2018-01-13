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
package org.icemoon.meson.cdt.language.settings.providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.icemoon.meson.MesonPlugin;

/**
 * A ILanguageSettingsProvider that parses the file 'build.ninja' produced by
 * meson.<br>
 * NOTE: This class misuses interface ICBuildOutputParser to detect when a build
 * did finish.<br>
 * NOTE: This class misuses interface ICListenerAgent to populate the
 * {@link #getSettingEntries setting entries} on workbench startup.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class NinjaParser extends AbstractParser
		implements ILanguageSettingsEditableProvider, ICListenerAgent, ICBuildOutputParser, Cloneable {

	private static final String WORKBENCH_WILL_NOT_KNOW_ALL_MSG = "Your workbench will not know all include paths and preprocessor defines.";
	private static final String MARKER_ID = MesonPlugin.PLUGIN_ID + ".NinjaParserMarker";

	public NinjaParser() {
		super(MARKER_ID, NinjaParser.class.getName());
	}

	/**
	 * Parses the content of the 'build.ninja' file corresponding to the specified
	 * configuration, if timestamps differ.
	 *
	 * @param initializingWorkbench
	 *            {@code true} if the workbench is starting up. If {@code true},
	 *            this method will not trigger UI update to show newly detected
	 *            include paths nor will it complain if a "build.ninja" file does
	 *            not exist.
	 * @throws CoreException
	 */
	protected void tryParse(boolean initializingWorkbench) throws CoreException {

		// If getBuilderCWD() returns a workspace relative path, it is garbled.
		// It returns '${workspace_loc:/my-project-name}'. Additionally, it returns
		// null on a project with makeNature.
		// In contrast, getResolvedOutputDirectories() does it mostly right, it
		// returns '/my-project-name', but also stale data
		// when a user changed the buil-root
		ICBuildSetting buildSetting = currentCfgDescription.getBuildSetting();
		final IPath buildRoot = buildSetting.getBuilderCWD();
		final IPath ninjaPath = buildRoot.append("build.ninja");
		final IFile ninjaFileRc = ResourcesPlugin.getWorkspace().getRoot().getFile(ninjaPath);

		final IPath location = ninjaFileRc.getLocation();
		if (location != null) {
			final File jsonFile = location.toFile();
			if (jsonFile.exists()) {
				// file exists on disk...
				final long tsJsonModified = jsonFile.lastModified();

				final IProject project = currentCfgDescription.getProjectDescription().getProject();
				final TimestampedLanguageSettingsStorage store = storage.getSettingsForConfig(currentCfgDescription);

				if (store.lastModified < tsJsonModified) {
					// must parse json file...
					// store time-stamp
					store.lastModified = tsJsonModified;
					store.clear();
					if (!initializingWorkbench) {
						project.deleteMarkers(MARKER_ID, false, IResource.DEPTH_INFINITE);
					}
					try {
						// parse file...
						BufferedReader in = new BufferedReader(new FileReader(jsonFile));
						try {
							String line = null;
							String rule = null;

							/* The actual C/C++ compilers */
							String cCompiler = "cc";
							String cppCompiler = "c++";

							/* The actual C/C++ linker */
							String cLinker = "cc";
							String cppLinker = "c++";

							/* State */
							String executable = null;
							String linker = null;

							while ((line = in.readLine()) != null) {
								line = line.trim();
								if (line.length() > 0) {
									List<String> args = ToolArgumentParsers.parseQuotedString(line);

									if (executable != null) {
										if (args.get(0).equals("LINK_ARGS")) {
											ParserDetection.ParserDetectionResult pdr = fastDetermineDetector(
													linker + " ");
											if (pdr == null) {
												// no matching parser found
												String message = "No parser for '" + line + "'. "
														+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
												createMarker(ninjaFileRc, message);
											} else {
												processCommandLine(store,
														pdr.getDetectorWithMethod().getDetector().getParser(),
														ninjaFileRc, ninjaFileRc.getParent().getLocation(),
														line.substring(line.indexOf('=') + 1).trim(),
														project.getLocation());
											}
										}
										executable = null;
										linker = null;
									} else {
										executable = null;
										linker = null;
										if (args.get(0).equals("rule")) {
											rule = args.get(1);
										} else if ("c_COMPILER".equals(rule) && args.get(0).equals("deps")) {
											cCompiler = args.get(2);
										} else if ("cpp_COMPILER".equals(rule) && args.get(0).equals("deps")) {
											cppCompiler = args.get(2);
										} else if ("c_LINKER".equals(rule) && args.get(0).equals("command")) {
											cLinker = args.get(2);
										} else if ("cpp_LINKER".equals(rule) && args.get(0).equals("command")) {
											cppLinker = args.get(2);
										} else if (args.get(0).equals("build") && args.size() > 1
												&& args.get(2).equals("cpp_LINKER")) {
											executable = args.get(1);
											linker = cppLinker;
											if (executable.endsWith(":"))
												executable = executable.substring(0, executable.length() - 1);
										}
									}
								}
							}
						} finally {
							in.close();
						}

						CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(project));

					} catch (Exception ex) {
						final String msg = "Failed to read file " + jsonFile + ". " + WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
						createMarker(ninjaFileRc, msg);
					}
				}
				return;
			}
		}
		if (!initializingWorkbench) {
			// no json file was produced in the build
			final String msg = "File '" + ninjaPath + "' was not created in the build. "
					+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
			createMarker(ninjaFileRc, msg);
		}
	}

	@Override
	public NinjaParser clone() throws CloneNotSupportedException {
		return (NinjaParser) super.clone();
	}

	@Override
	public NinjaParser cloneShallow() throws CloneNotSupportedException {
		return (NinjaParser) super.cloneShallow();
	}

	private void processCommandLine(TimestampedLanguageSettingsStorage storage, IToolCommandlineParser cmdlineParser,
			IFile sourceFile, IPath cwd, String line, IPath project) {
		line = line.trim();
		final List<ICLanguageSettingEntry> entries = cmdlineParser.processArgs(cwd, line, project);
		// attach settings to sourceFile resource...
		System.out.println("processCommandLine ONE! " + line + " for " + sourceFile);
		if (entries != null && entries.size() > 0) {
			for (ICLanguageSettingEntry entry : entries) {
				System.out.println("GOT ONE! " + entry);
				if (entry.getKind() == ICSettingEntry.LIBRARY_PATH) {
					storage.setSettingEntries((String) null, cmdlineParser.getLanguageId(), entries);
				}
			}
			storage.setSettingEntries(sourceFile, cmdlineParser.getLanguageId(), entries);
		}
	}
}
