/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *    Emerald Icemoon - Ported to Ninja
 *******************************************************************************/

package org.icemoon.cdt.ninja.core.corebuild.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.icemoon.cdt.ninja.core.ArgumentLine;
import org.icemoon.cdt.ninja.core.IRuleDef;
import org.icemoon.cdt.ninja.core.NinjaFile;
import org.icemoon.cdt.ninja.core.NinjaPlugin;
import org.icemoon.cdt.ninja.core.language.settings.providers.CompileCommand;
import org.icemoon.cdt.ninja.core.language.settings.providers.Messages;

import com.google.gson.Gson;

public class NinjaBuildConfiguration extends CBuildConfiguration {
	public static final String BUILD_COMMAND = "ninja.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "ninja.command.clean"; //$NON-NLS-1$
	public static final String USE_CUSTOM_COMMANDS = "ninja.custom";

	public NinjaBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public NinjaBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		super(config, name, toolChain, "run"); // TODO: why "run" //$NON-NLS-1$
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {

		IProject project = getProject();
		try {

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			outStream.write(String.format(Messages.NinjaBuildConfiguration_BuildingIn, buildDir.toString()));

			String buildCommand = getProperty(BUILD_COMMAND);
			if (buildCommand == null) {
				buildCommand = "ninja"; //$NON-NLS-1$
			}

			getBuildContainer(); // Creates it. TODO. needs to create more than just two levels if path becomes
									// configurable
			List<String> buildArgs = new ArgumentLine(buildCommand);
			List<String> cmdargs = normalizeCommand(buildArgs);

			execute(cmdargs, buildDir, console, monitor); // $NON-NLS-1$

			/* Arguments */
			List<String> ccargs = new ArrayList<String>(Arrays.asList(cmdargs.get(0), "-t", "compdb"));

			/* Extract the rules */
			NinjaFile nf = new NinjaFile();
			File ninjaOsFile = new File(project.getFile("build.ninja").getLocation().toOSString());
			try (Reader reader = new FileReader(ninjaOsFile)) {
				nf.parse(ninjaOsFile.toURI(), reader);
				for (IRuleDef r : nf.getRuleDefs()) {
					ccargs.add(r.getName());
				}
			} catch (IOException e) {
				throw new CoreException(NinjaPlugin.errorStatus(
						String.format(Messages.NinjaBuildConfiguration_ProcCompCmds, getProject().getName()), e));
			}

			/* Generate it */
			ProcessBuilder pb = new ProcessBuilder(normalizeCommand(ccargs));
			pb.directory(ninjaOsFile.getParentFile());
			pb.redirectErrorStream(true);
			try {
				Process p = pb.start();
				try {
					try (Reader reader = new InputStreamReader(p.getInputStream())) {
						Gson gson = new Gson();
						CompileCommand[] ccommands = gson.fromJson(reader, CompileCommand[].class);
						Map<String, CompileCommand> dedupedCmds = new HashMap<>();
						for (CompileCommand ccommand : ccommands) {
							dedupedCmds.put(ccommand.getFile(), ccommand);
						}
						for (CompileCommand ccommand : dedupedCmds.values()) {
							ArgumentLine l = new ArgumentLine();
							processLine(l.parse(ccommand.getCommand()).toString());
						}
					} catch (IOException e) {
						throw new CoreException(NinjaPlugin.errorStatus(
								String.format(Messages.NinjaBuildConfiguration_ProcCompCmds, getProject().getName()),
								e));
					}
				} finally {
					if (p.waitFor() != 0) {
						throw new IOException("ninja -t commands failed with exit code " + p.exitValue());
					}
				}
			} catch (Exception ie) {
				throw new CoreException(NinjaPlugin.errorStatus(
						String.format(Messages.NinjaBuildConfiguration_ProcCompCmds, getProject().getName()), ie));
			}

			getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

			outStream.write(String.format(Messages.NinjaBuildConfiguration_BuildingComplete, buildDir.toString()));

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(NinjaPlugin
					.errorStatus(String.format(Messages.NinjaBuildConfiguration_Building, project.getName()), e));
		}
	}

	@Override
	public URI getBuildDirectoryURI() throws CoreException {
		return getProject().getLocationURI();
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		Path buildDir = getBuildDirectory();
		String cleanCommand = getProperty(CLEAN_COMMAND);
		if (cleanCommand == null) {
			cleanCommand = "ninja clean"; //$NON-NLS-1$
		}
		execute(normalizeCommand(new ArgumentLine(cleanCommand)), buildDir, console, monitor);
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	protected void execute(List<String> command, Path dir, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		command = normalizeCommand(command);
		ProcessBuilder builder = new ProcessBuilder(command).directory(dir.toFile());
		setBuildEnvironment(builder.environment());
		try {
			try (ErrorParserManager epm = new ErrorParserManager(getProject(), getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());

				Process process = builder.start();
				watchProcess(process, new IConsoleParser[] { epm });
			}
		} catch (IOException e) {
			throw new CoreException(NinjaPlugin
					.errorStatus(String.format(Messages.NinjaBuildConfiguration_Building, getProject().getName()), e));
		}
	}

	private List<String> normalizeCommand(List<String> command) {
		String cmd = command.get(0);

		if (Platform.getOS().equals(Platform.OS_WIN32) && !(cmd.endsWith(".exe") && !cmd.endsWith(".bat"))) { //$NON-NLS-1$ //$NON-NLS-2$
			// Maybe a shell script, see if we can launch it in sh
			// TODO this probably should be generalized in CBuildConfiguration
			Path shPath = findCommand("sh"); //$NON-NLS-1$
			if (shPath != null) {
				List<String> shCommand = new ArrayList<>();
				shCommand.add(shPath.toString());
				shCommand.add("-c"); //$NON-NLS-1$
				shCommand.add("\"" + String.join(" ", command) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				command = shCommand;
			}
		} else {
			Path cmdPath = findCommand(cmd);
			if (cmdPath != null) {
				cmd = cmdPath.toString();
				command.set(0, cmd);
			}
		}
		return command;
	}

}
