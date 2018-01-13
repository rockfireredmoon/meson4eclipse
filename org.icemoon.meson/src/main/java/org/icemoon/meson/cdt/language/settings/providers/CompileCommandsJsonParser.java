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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jetty.util.ajax.JSON;
import org.icemoon.meson.MesonPlugin;

/**
 * A ILanguageSettingsProvider that parses the file 'compile_commands.json'
 * produced by meson.<br>
 * NOTE: This class misuses interface ICBuildOutputParser to detect when a build
 * did finish.<br>
 * NOTE: This class misuses interface ICListenerAgent to populate the
 * {@link #getSettingEntries setting entries} on workbench startup.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class CompileCommandsJsonParser extends AbstractParser
		implements ILanguageSettingsEditableProvider, ICListenerAgent, ICBuildOutputParser, Cloneable {

	private static final String WORKBENCH_WILL_NOT_KNOW_ALL_MSG = "Your workbench will not know all include paths and preprocessor defines.";

	private static final String MARKER_ID = MesonPlugin.PLUGIN_ID + ".CompileCommandsJsonParserMarker";

	public CompileCommandsJsonParser() {
		super(MARKER_ID, CompileCommandsJsonParser.class.getName());
	}

	/**
	 * Parses the content of the 'compile_commands.json' file corresponding to the
	 * specified configuration, if timestamps differ.
	 *
	 * @param initializingWorkbench
	 *            {@code true} if the workbench is starting up. If {@code true},
	 *            this method will not trigger UI update to show newly detected
	 *            include paths nor will it complain if a "compile_commands.json"
	 *            file does not exist.
	 * @throws CoreException
	 */
	protected void xxTryParse(boolean initializingWorkbench) throws CoreException {

		// If getBuilderCWD() returns a workspace relative path, it is garbled.
		// It returns '${workspace_loc:/my-project-name}'. Additionally, it returns
		// null on a project with makeNature.
		// In contrast, getResolvedOutputDirectories() does it mostly right, it
		// returns '/my-project-name', but also stale data
		// when a user changed the buil-root
		final IPath buildRoot = currentCfgDescription.getBuildSetting().getBuilderCWD();
		final IPath jsonPath = buildRoot.append("compile_commands.json");
		final IFile jsonFileRc = ResourcesPlugin.getWorkspace().getRoot().getFile(jsonPath);

		final IPath location = jsonFileRc.getLocation();
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
						JSON parser = new JSON();
						Reader in = new FileReader(jsonFile);
						try {
							Object parsed = parser.parse(new JSON.ReaderSource(in), false);
							if (parsed instanceof Object[]) {
								for (Object o : (Object[]) parsed) {
									if (o instanceof Map) {
										processJsonEntry(store, (Map<?, ?>) o, jsonFileRc, project.getLocation());
									} else {
										// expected Map object, skipping entry.toString()
										final String msg = "File format error: unexpected entry '" + o + "'. "
												+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
										createMarker(jsonFileRc, msg);
									}
								}
								// System.out.println("stored cached compile_commands");

								// re-index to reflect new paths and macros in editor views
								// serializeLanguageSettings(currentCfgDescription);
								CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(project));

								// trigger UI update to show newly detected include paths in
								// Includes folder
								// Useless. It looks like ICProject#getIncludeReferences() is only
								// updated when the project is opened.
								// Display.getDefault().asyncExec(new Runnable() {
								// public void run() {
								// ProjectExplorer projectExplorer = (ProjectExplorer)
								// PlatformUI.getWorkbench()
								// .getActiveWorkbenchWindow().getActivePage().findView(
								//// "org.eclipse.cdt.ui.CView");
								// ProjectExplorer.VIEW_ID);
								// if (projectExplorer != null) {
								// final CommonViewer v = projectExplorer.getCommonViewer();
								// if (v != null) {
								// v.refresh(project);
								// }
								// }
								// }
								// });

							} else {
								// file format error
								final String msg = "File does not seem to be in JSON format. "
										+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
								createMarker(jsonFileRc, msg);
							}
						} finally {
							in.close();
						}
					} catch (IOException ex) {
						final String msg = "Failed to read file " + jsonFile + ". " + WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
						createMarker(jsonFileRc, msg);
					}
				}
				return;
			}
		}
		if (!initializingWorkbench) {
			// no json file was produced in the build
			final String msg = "File '" + jsonPath + "' was not created in the build. "
					+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
			createMarker(jsonFileRc, msg);
		}
	}
	/**
	 * Parses the content of the 'compile_commands.json' file corresponding to the
	 * specified configuration, if timestamps differ.
	 *
	 * @param initializingWorkbench
	 *            {@code true} if the workbench is starting up. If {@code true},
	 *            this method will not trigger UI update to show newly detected
	 *            include paths nor will it complain if a "compile_commands.json"
	 *            file does not exist.
	 * @throws CoreException
	 */
	protected void tryParse(boolean initializingWorkbench) throws CoreException {
		xxTryParse(initializingWorkbench);
		// If getBuilderCWD() returns a workspace relative path, it is garbled.
		// It returns '${workspace_loc:/my-project-name}'. Additionally, it returns
		// null on a project with makeNature.
		// In contrast, getResolvedOutputDirectories() does it mostly right, it
		// returns '/my-project-name', but also stale data
		// when a user changed the buil-root
		final IPath buildRoot = currentCfgDescription.getBuildSetting().getBuilderCWD();
		final IPath jsonPath = buildRoot.append("compile_commands.json");
		final IFile jsonFileRc = ResourcesPlugin.getWorkspace().getRoot().getFile(jsonPath);

		final IPath location = jsonFileRc.getLocation();
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
						JSON parser = new JSON();
						Reader in = new FileReader(jsonFile);
						try {
							Object parsed = parser.parse(new JSON.ReaderSource(in), false);
							if (parsed instanceof Object[]) {
								for (Object o : (Object[]) parsed) {
									if (o instanceof Map) {
										processJsonEntry(store, (Map<?, ?>) o, jsonFileRc, project.getLocation());
									} else {
										// expected Map object, skipping entry.toString()
										final String msg = "File format error: unexpected entry '" + o + "'. "
												+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
										createMarker(jsonFileRc, msg);
									}
								}
								// System.out.println("stored cached compile_commands");

								// re-index to reflect new paths and macros in editor views
								// serializeLanguageSettings(currentCfgDescription);
								CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(project));

								// trigger UI update to show newly detected include paths in
								// Includes folder
								// Useless. It looks like ICProject#getIncludeReferences() is only
								// updated when the project is opened.
								// Display.getDefault().asyncExec(new Runnable() {
								// public void run() {
								// ProjectExplorer projectExplorer = (ProjectExplorer)
								// PlatformUI.getWorkbench()
								// .getActiveWorkbenchWindow().getActivePage().findView(
								//// "org.eclipse.cdt.ui.CView");
								// ProjectExplorer.VIEW_ID);
								// if (projectExplorer != null) {
								// final CommonViewer v = projectExplorer.getCommonViewer();
								// if (v != null) {
								// v.refresh(project);
								// }
								// }
								// }
								// });

							} else {
								// file format error
								final String msg = "File does not seem to be in JSON format. "
										+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
								createMarker(jsonFileRc, msg);
							}
						} finally {
							in.close();
						}
					} catch (IOException ex) {
						final String msg = "Failed to read file " + jsonFile + ". " + WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
						createMarker(jsonFileRc, msg);
					}
				}
				return;
			}
		}
		if (!initializingWorkbench) {
			// no json file was produced in the build
			final String msg = "File '" + jsonPath + "' was not created in the build. "
					+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
			createMarker(jsonFileRc, msg);
		}
	}

	/**
	 * Processes an entry from a {@code compile_commands.json} file and stores a
	 * {@link ICLanguageSettingEntry} for the file given the specified map.
	 *
	 * @param storage
	 *            where to store language settings
	 * @param sourceFileInfo
	 *            a Map of type Map<String,String>
	 * @param jsonFile
	 *            the JSON file being parsed (for marker creation only)
	 * @throws CoreException
	 *             if marker creation failed
	 */
	private void processJsonEntry(TimestampedLanguageSettingsStorage storage, Map<?, ?> sourceFileInfo, IFile jsonFile,
			IPath project) throws CoreException {

		if (sourceFileInfo.containsKey("file") && sourceFileInfo.containsKey("command")
				&& sourceFileInfo.containsKey("directory")) {
			final String file = sourceFileInfo.get("file").toString();
			if (file != null && !file.isEmpty()) {
				final String cmdLine = sourceFileInfo.get("command").toString();
				if (cmdLine != null && !cmdLine.isEmpty()) {
					final String cwdStr = sourceFileInfo.get("directory").toString();
					IContainer dir = jsonFile.getParent();
					IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(dir.getFullPath());
					IPath cwd = cwdStr != null ? Path.fromOSString(cwdStr) : folder.getLocation();
					IFile ifile = folder.getFile(file);
					if (ifile.exists()) {
						ParserDetection.ParserDetectionResult pdr = fastDetermineDetector(cmdLine);
						if (pdr != null) {
							processCommandLine(storage, pdr.getDetectorWithMethod().getDetector().getParser(), ifile,
									cwd, pdr.getReducedCommandLine(), project);
						} else {
							// no matching parser found
							String message = "No parser for command '" + cmdLine + "'. "
									+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
							createMarker(jsonFile, message);
						}
					}
					return;
				}
			}
		}
		// unrecognized entry, skipping
		final String msg = "File format error: " + ": 'file', 'command' or 'directory' missing in JSON object. "
				+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
		createMarker(jsonFile, msg);
	}

	/**
	 * Processes the command-line of an entry from a {@code compile_commands.json}
	 * file by trying the specified detector and stores a
	 * {@link ICLanguageSettingEntry} for the file found in the specified map.
	 *
	 * @param storage
	 *            where to store language settings
	 * @param cmdlineParser
	 *            the tool detector and its tool option parsers
	 * @param sourceFile
	 *            the source file resource corresponding to the source file being
	 *            processed by the tool
	 * @param cwd
	 *            the current working directory of the compiler at its invocation
	 * @param line
	 *            the command line to process
	 */
	private void processCommandLine(TimestampedLanguageSettingsStorage storage, IToolCommandlineParser cmdlineParser,
			IFile sourceFile, IPath cwd, String line, IPath project) {
		line = line.trim();
		final List<ICLanguageSettingEntry> entries = cmdlineParser.processArgs(cwd, line, project);
		// attach settings to sourceFile resource...
		if (entries != null && entries.size() > 0) {
			for (ICLanguageSettingEntry entry : entries) {
				if (entry.getKind() == ICSettingEntry.INCLUDE_PATH) {
					/*
					 * compile_commands.json holds entries per-file only and does not contain
					 * per-project or per-folder entries. For include dirs, ALSO add these entries
					 * to the project resource to make them show up in the UI in the includes
					 * folder...
					 */
					storage.setSettingEntries((String) null, cmdlineParser.getLanguageId(), entries);
				}
			}
			storage.setSettingEntries(sourceFile, cmdlineParser.getLanguageId(), entries);
		}
	}

	@Override
	public CompileCommandsJsonParser clone() throws CloneNotSupportedException {
		return (CompileCommandsJsonParser) super.clone();
	}

	@Override
	public CompileCommandsJsonParser cloneShallow() throws CloneNotSupportedException {
		return (CompileCommandsJsonParser) super.cloneShallow();
	}

}
