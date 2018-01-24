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
package org.icemoon.cdt.ninja.core.language.settings.providers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.icemoon.cdt.ninja.core.IRuleDef;
import org.icemoon.cdt.ninja.core.NinjaFile;
import org.icemoon.cdt.ninja.core.NinjaPlugin;

import com.google.gson.Gson;

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

	private static final String MARKER_ID = NinjaPlugin.PLUGIN_ID + ".CompileCommandsJsonParserMarker";

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
	@Override
	protected void tryParse(boolean initializingWorkbench) throws CoreException {

		IPath pathBuilderCWD = currentCfgDescription.getBuildSetting().getBuilderCWD();
		String builderCWD = pathBuilderCWD.toString();
		try {
			// here is a hack to overcome ${workspace_loc:/prj-name} returned by builder
			// where "/" is treated as path separator by pathBuilderCWD
			ICdtVariableManager vmanager = CCorePlugin.getDefault().getCdtVariableManager();
			builderCWD = vmanager.resolveValue(builderCWD, "", null, currentCfgDescription); //$NON-NLS-1$
			pathBuilderCWD = new Path(builderCWD);
		} catch (CdtVariableException e) {
			NinjaPlugin.log(e);
		}

		doDir(initializingWorkbench, pathBuilderCWD);
	}

	protected boolean doDir(boolean initializingWorkbench, IPath pathBuilderCWD) throws CoreException {

		final IPath ninjaBuildPath = pathBuilderCWD.append("build.ninja");
		final IPath ninjaBuildRel = ninjaBuildPath
				.makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation());
		final IFile ninjaBuildFile = ResourcesPlugin.getWorkspace().getRoot().getFile(ninjaBuildRel);

		boolean ok = false;

		final IPath location = ninjaBuildFile.getLocation();
		if (location != null) {
			final File ninjaFile = location.toFile();
			if (ninjaFile.exists()) {

				final long ninjaModified = ninjaFile.lastModified();

				final IProject project = currentCfgDescription.getProjectDescription().getProject();
				final TimestampedLanguageSettingsStorage store = storage.getSettingsForConfig(currentCfgDescription);

				if (store.lastModified < ninjaModified) {
					store.lastModified = ninjaModified;
					store.clear();
					if (!initializingWorkbench) {
						project.deleteMarkers(MARKER_ID, false, IResource.DEPTH_INFINITE);
					}

					/* Arguments */
					List<String> args = new ArrayList<String>(Arrays.asList("ninja", "-t", "compdb"));

					/* Extract the rules */
					NinjaFile nf = new NinjaFile();
					try (Reader reader = new FileReader(ninjaFile)) {
						nf.parse(ninjaFile.toURI(), reader);
						for (IRuleDef r : nf.getRuleDefs()) {
							args.add(r.getName());
						}
					} catch (IOException e) {
						throw new CoreException(NinjaPlugin.errorStatus(
								String.format(Messages.NinjaBuildConfiguration_ProcCompCmds, project.getName()), e));
					}

					/* Generate it */
					ProcessBuilder pb = new ProcessBuilder(args);
					pb.directory(ninjaFile.getParentFile());
					pb.redirectErrorStream(true);
					try {
						Process p = pb.start();
						try {
							try (Reader reader = new InputStreamReader(p.getInputStream())) {
								Gson gson = new Gson();
								CompileCommand[] commands = gson.fromJson(reader, CompileCommand[].class);
								Map<String, CompileCommand> dedupedCmds = new HashMap<>();
								for (CompileCommand command : commands) {
									dedupedCmds.put(command.getFile(), command);
								}
								for (CompileCommand command : dedupedCmds.values()) {
									processJsonEntry(store, command, ninjaBuildFile, project.getLocation());
								}
							} catch (IOException e) {
								throw new CoreException(NinjaPlugin.errorStatus(
										String.format(Messages.NinjaBuildConfiguration_ProcCompCmds, project.getName()),
										e));
							}
						} finally {
							if (p.waitFor() != 0) {
								throw new IOException("ninja -t commands failed with exit code " + p.exitValue());
							}
						}
					} catch (Exception ie) {
						NinjaPlugin.log(ie);
						createMarker(ninjaBuildFile, "Failed to regenerate compile commands. " + ie.getMessage());
					}

					CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(project));
				}

				ok = true;
			}
		}


		if (!initializingWorkbench && !ok) {
			// no json file was produced in the build
			final String msg = "Could not detect compilere commands for project at " + pathBuilderCWD
					+ WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
			createMarker(ninjaBuildFile, msg);
		}
		
		return ok;
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
	private void processJsonEntry(TimestampedLanguageSettingsStorage storage, CompileCommand command, IFile jsonFile,
			IPath project) throws CoreException {

		final String file = command.getFile();
		if (file != null && !file.isEmpty()) {
			final String cmdLine = command.getCommand();
			if (cmdLine != null && !cmdLine.isEmpty()) {
				final String cwdStr = command.getDirectory();
				
				/* The working directory */
				IPath dir = jsonFile.getParent().getLocation();
				if(cwdStr != null) {
					dir = Path.fromOSString(cwdStr);
					if(!dir.isAbsolute()) {
						dir = jsonFile.getParent().getLocation().append(dir);
					}
				}
				
				/* The file */
				IPath filePath = Path.fromOSString(file);
				if(!filePath.isAbsolute())
					filePath = jsonFile.getParent().getLocation().append(filePath);
				
				
				/* Relative to workspace */
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				IPath workspaceFilePath = filePath.makeRelativeTo(workspaceRoot.getLocation());
				IFile ifile = workspaceRoot.getFile(workspaceFilePath);
				
				/* Parse */
				if (ifile.exists()) {
					ParserDetection.ParserDetectionResult pdr = fastDetermineDetector(cmdLine);
					if (pdr != null) {
						processCommandLine(storage, pdr.getDetectorWithMethod().getDetector().getParser(), ifile, dir,
								pdr.getReducedCommandLine(), project);
					} else {
						// no matching parser found
						String message = "No parser for command '" + cmdLine + "'. " + WORKBENCH_WILL_NOT_KNOW_ALL_MSG;
						createMarker(jsonFile, message);
					}
				}
				return;
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
