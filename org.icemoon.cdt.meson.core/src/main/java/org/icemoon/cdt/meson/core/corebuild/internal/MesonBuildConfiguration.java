/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Emerald Icemoon - Ported to Meson
 *******************************************************************************/
package org.icemoon.cdt.meson.core.corebuild.internal;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.ArgumentLine;
import org.icemoon.cdt.meson.core.IMesonCrossCompileFile;
import org.icemoon.cdt.meson.core.IMesonCrossCompileManager;
import org.icemoon.cdt.meson.core.MesonBackend;
import org.icemoon.cdt.meson.core.MesonErrorParser;
import org.icemoon.cdt.meson.core.internal.CompileCommand;
import org.icemoon.cdt.meson.core.internal.MesonHelper;
import org.icemoon.cdt.ninja.core.corebuild.NinjaBuildConfiguration;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;

public class MesonBuildConfiguration extends CBuildConfiguration {

	private static final String TOOLCHAIN_FILE = "cdt.meson.toolchainfile"; //$NON-NLS-1$

	private IMesonCrossCompileFile crossCompileFile;
	private MesonHelper helper;

	public MesonBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		IMesonCrossCompileManager manager = Activator.getService(IMesonCrossCompileManager.class);
		Preferences settings = getSettings();
		String pathStr = settings.get(TOOLCHAIN_FILE, ""); // $NON-NL-4$ //$NON-NLS-1$
		if (!pathStr.isEmpty()) {
			Path path = Paths.get(pathStr);
			crossCompileFile = manager.getToolChainFile(path);
		} else {
			crossCompileFile = manager.getToolChainFileFor(getToolChain());
			if (crossCompileFile != null) {
				saveToolChainFile();
			}
		}
	}

	public MesonBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		this(config, name, toolChain, null, "run"); // $NON-NLS-5$ //$NON-NLS-1$
	}

	public MesonBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			IMesonCrossCompileFile crossCompileFile, String launchMode) {
		super(config, name, toolChain, launchMode);

		this.crossCompileFile = crossCompileFile;
		if (crossCompileFile != null) {
			saveToolChainFile();
		}

		helper = new MesonHelper(config.getName(), null, crossCompileFile, toolChain, config.getProject(), null);
	}

	private void saveToolChainFile() {
		Preferences settings = getSettings();
		settings.put(TOOLCHAIN_FILE, crossCompileFile.getPath().toString());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			project.deleteMarkers(MesonErrorParser.MESON_PROBLEM_MARKER_ID, false, IResource.DEPTH_INFINITE);
			ConsoleOutputStream outStream = console.getOutputStream();
			IPath buildDir = helper.getBuildFolder().getLocation();
			if (helper.shouldGenerate()) {

				IStatus status = null;
				try (ErrorParserManager epm = new ErrorParserManager(project, project.getLocationURI(), this,
						new String[] { MesonErrorParser.ID })) {
					epm.setOutputStream(console.getErrorStream());
					Process process = helper.regenerateMakefiles(monitor, console);
					if (process == null)
						throw new CoreException(new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
								Messages.MesonBuildConfiguration_2, null));
					watchProcess(process, new IConsoleParser[] { epm });
					// check meson exit status
					final int exitValue = process.exitValue();
					if (exitValue != 0) {
						// meson had errors...
						status = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, String.format(
								Messages.MesonBuildConfiguration_3, helper.getBuildCommand(null), exitValue), null);
					}
				} catch (IOException ioe) {
					throw new CoreException(new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
							Messages.MesonBuildConfiguration_4, ioe));
				}

				if (status != null && status.getCode() != IStatus.OK) {
					return new IProject[] { project };
				}
			}

			// TODO getLaunchMode() - make sure meson is in debug mode if launching as
			// debug?

			if (helper.getBackend() == MesonBackend.NINJA) {
				NinjaBuildConfiguration ninjaCfg = createNinjaConfiguration();
				ninjaCfg.build(kind, args, console, monitor);
			} else {
				outStream.write(String.format(Messages.MesonBuildConfiguration_BuildingIn, buildDir.toString()));
				List<String> epis = new ArrayList<String>(Arrays.asList(getToolChain().getErrorParserIds()));
				if (!epis.contains(MesonErrorParser.ID))
					epis.add(MesonErrorParser.ID);
				try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
						epis.toArray(new String[0]))) {
					epm.setOutputStream(console.getErrorStream());

					String buildCommand = helper.getBuildCommand(null);
					ArgumentLine argumentLine = new ArgumentLine(buildCommand);
					Path cmdPath = findCommand(argumentLine.get(0));
					if (cmdPath != null) {
						argumentLine.set(0, cmdPath.toString());
					}
					ProcessBuilder processBuilder = new ProcessBuilder(argumentLine).directory(buildDir.toFile());
					setBuildEnvironment(processBuilder.environment());
					Process process = processBuilder.start();
					outStream.write(String.join(" ", argumentLine) + '\n'); //$NON-NLS-1$
					watchProcess(process, new IConsoleParser[] { epm });

					outStream.write(
							String.format(Messages.MesonBuildConfiguration_BuildingComplete, buildDir.toString()));
				}

			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// Load compile_commands.json file
			processCompileCommandsFile(monitor);

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.MesonBuildConfiguration_Building, project.getName()), e));
		}
	}

	private Map<String, String> getBuildEnv() {
		Map<String, String> env = new HashMap<>();
		setBuildEnvironment(env);
		return env;
	}

	public Path getProjectDirectory() throws CoreException {
		return Paths.get(getProject().getLocation().toOSString());
	}

	@Override
	public IContainer getBuildContainer() throws CoreException {
		return helper.getBuildFolder();
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {

		if (helper.getBackend() == MesonBackend.NINJA) {
			NinjaBuildConfiguration ninjaCfg = createNinjaConfiguration();
			ninjaCfg.clean(console, monitor);
		} else {
			IProject project = getProject();
			try {
				project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
				ConsoleOutputStream outStream = console.getOutputStream();
				if (!helper.getBuildFolder().exists()) {
					outStream.write(Messages.MesonBuildConfiguration_NotFound);
					return;
				}
				ArgumentLine argumentLine = new ArgumentLine(helper.getBackend().getCleanCommand());
				Path cmdPath = findCommand(argumentLine.get(0));
				if (cmdPath != null) {
					argumentLine.set(0, cmdPath.toString());
				}
				helper.build(monitor, console, getBuildEnv(), argumentLine.toArray(new String[0]));
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (IOException e) {
				throw new CoreException(Activator
						.errorStatus(String.format(Messages.MesonBuildConfiguration_Cleaning, project.getName()), e));
			}
		}
	}

	private NinjaBuildConfiguration createNinjaConfiguration() throws CoreException {
		return new NinjaBuildConfiguration(getBuildConfiguration(), getName()) {
			@Override
			public URI getBuildDirectoryURI() throws CoreException {
				return MesonBuildConfiguration.this.getBuildDirectoryURI();
			}
		};
	}

	private void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile)) {
			monitor.setTaskName(Messages.MesonBuildConfiguration_ProcCompJson);
			try (FileReader reader = new FileReader(commandsFile.toFile())) {
				Gson gson = new Gson();
				CompileCommand[] commands = gson.fromJson(reader, CompileCommand[].class);
				Map<String, CompileCommand> dedupedCmds = new HashMap<>();
				for (CompileCommand command : commands) {
					dedupedCmds.put(command.getFile(), command);
				}
				ArgumentLine l = new ArgumentLine();
				for (CompileCommand command : dedupedCmds.values()) {
					processLine(l.parse(command.getCommand()).toString());
				}
				shutdown();
			} catch (IOException e) {
				throw new CoreException(Activator.errorStatus(
						String.format(Messages.MesonBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}

}
