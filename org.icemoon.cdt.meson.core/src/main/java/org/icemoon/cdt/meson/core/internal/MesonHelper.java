package org.icemoon.cdt.meson.core.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.ArgumentLine;
import org.icemoon.cdt.meson.core.IMesonCrossCompileFile;
import org.icemoon.cdt.meson.core.IMesonCrossCompileManager;
import org.icemoon.cdt.meson.core.MesonBackend;
import org.icemoon.cdt.meson.core.ProjectOption;
import org.icemoon.cdt.meson.core.corebuild.internal.Messages;
import org.icemoon.cdt.meson.core.settings.AbstractOsPreferences;
import org.icemoon.cdt.meson.core.settings.ConfigurationManager;
import org.icemoon.cdt.meson.core.settings.MesonPreferences;

public class MesonHelper {
	private static final ILog log = Activator.getDefault().getLog();

	/** buildscript generation error marker ID */
	private static final String MARKER_ID = Activator.PLUGIN_ID + ".BuildscriptGenerationError";

	private ICConfigurationDescription cfgd;
	private IConfiguration config;
	private IProject project;
	private IToolChain toolChain;
	private ICommandLauncher launcher;
	private String configName;
	private IMesonCrossCompileFile crossCompileFile;

	public MesonHelper(String configName, ICommandLauncher launcher, IMesonCrossCompileFile crossCompileFile,
			IToolChain toolChain, IProject project, ICConfigurationDescription cfgd, IConfiguration config) {

		if (launcher == null) {
			launcher = CommandLauncherManager.getInstance().getCommandLauncher(project);
			if (launcher == null)
				launcher = new CommandLauncher();
		}

		this.launcher = launcher;
		this.cfgd = cfgd;
		this.crossCompileFile = crossCompileFile;
		this.toolChain = toolChain;
		this.configName = configName;
		this.project = project;
		this.config = config;
	}

	public String getBuildArguments(String arguments) throws CoreException {

		if (arguments == null)
			arguments = "";

		ArgumentLine l = new ArgumentLine(getBuildScriptCommand());
		l.remove(0);

		MesonBackend backend = getBackend();
		final String ignoreErrOption = backend.getIgnoreErrOption();
		if (ignoreErrOption != null) {
			arguments = arguments.replaceAll("\\$\\{MesonBuildToolIgnErr\\}", ignoreErrOption);
		}
		l.addAll(new ArgumentLine(arguments));
		return l.toString();
	}

	public String getBuildCommand(String command) throws CoreException {
		if (command == null)
			command = "${MesonBuildTool}";
		return command.replace("${MesonBuildTool}", getBuildScriptCommand());
	}

	private String getBuildScriptCommand() throws CoreException {
		MesonPreferences prefs = ConfigurationManager.getInstance().getOrLoad(cfgd);
		final AbstractOsPreferences osPrefs = AbstractOsPreferences.extractOsPreferences(prefs);
		MesonBackend backend = getBackend();
		String prefCmd = osPrefs.getBuildscriptProcessorCommand();
		if (prefCmd == null || prefCmd.length() == 0)
			prefCmd = backend.getBuildscriptProcessorCommand();
		return prefCmd;
	}

	public MesonBackend getBackend() throws CoreException {

		MesonPreferences prefs = ConfigurationManager.getInstance().getOrLoad(cfgd);
		final AbstractOsPreferences osPrefs = AbstractOsPreferences.extractOsPreferences(prefs);

		MesonBackend backend = osPrefs.getBackend();
		if (backend == null) {
			// If getBuilderCWD() returns a workspace relative path, it gets garbled by CDT.
			// If garbled, make sure
			// org.icemoon.cdt.meson.core.internal.BuildscriptGenerator.getBuildWorkingDir()
			// returns a full, absolute path relative to the workspace.
			final IPath builderCWD = cfgd.getBuildSetting().getBuilderCWD();
			IFolder cwd = ResourcesPlugin.getWorkspace().getRoot().getFolder(builderCWD);

			IPath location = cwd.getLocation();
			if (location != null) {
				for (MesonBackend b : MesonBackend.values()) {
					if (cwd.getFile(b.getMakefileName()).exists()) {
						backend = b;
						break;
					}
				}
			}
		}
		if (backend == null)
			backend = MesonBackend.NINJA;
		return backend;
	}

	public IFolder getBuildFolder() {
		// set the top build dir path for the current configuration
		String buildDirStr = null;
		try {
			MesonPreferences prefs = ConfigurationManager.getInstance().getOrLoad(cfgd);
			buildDirStr = prefs.getBuildDirectory();
		} catch (CoreException e) {
			// storage base is null; treat as bug in CDT..
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "falling back to hard coded build directory", e));
		}
		IPath buildP;
		if (buildDirStr == null) {
			// not configured: fall back to legacy behavior
			buildP = new Path("builddir").append(configName);
		} else {
			buildP = new Path(buildDirStr);
		}

		// Note that IPath from ICBuildSetting#getBuilderCWD() holding variables is
		// mis-constructed,
		// i.e. ${workspace_loc:/path} gets split into 2 path segments.
		// MBS does that and we need to handle that

		// So resolve variables here and return an workspace relative path to not give
		// CTD a chance to garble it up..
		ICdtVariableManager mngr = CCorePlugin.getDefault().getCdtVariableManager();
		try {
			String buildPathString = buildP.toString();
			buildPathString = mngr.resolveValue(buildPathString, "", "", cfgd);
			buildP = new Path(buildPathString);
		} catch (CdtVariableException e) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "variable expansion for build directory failed", e));
		}

		return project.getFolder(buildP);
	}

	public Process regenerateMakefiles(IProgressMonitor monitor, IConsole console) throws CoreException, IOException {

		project.deleteMarkers(MARKER_ID, false, IResource.DEPTH_ZERO);

		// See if the user has cancelled the build
		checkCancel(monitor);

		final IFolder buildFolder = getBuildFolder();
		if (buildFolder.exists()) {
			buildFolder.delete(true, monitor);
			project.refreshLocal(IResource.DEPTH_ZERO, monitor);
		}

		// See if the user has cancelled the build
		checkCancel(monitor);

		updateMonitor("Execute Meson for " + project.getName(), monitor);
		try {
			final ConsoleOutputStream cis = console.getInfoStream();
			cis.write(SimpleDateFormat.getTimeInstance().format(new Date()).getBytes());
			cis.write(" Buildscript generation: ".getBytes());
			cis.write(project.getName().getBytes());
			cis.write("::".getBytes());
			cis.write(configName.getBytes());
			cis.write(" in ".getBytes());
			cis.write(project.getLocation().toString().getBytes());
			cis.write("\n".getBytes());
		} catch (IOException ignore) {
		}

		checkCancel(monitor);

		return invokeMeson(buildFolder.getLocation(), console, monitor);
	}

	private void updateMonitor(String msg, IProgressMonitor monitor) {
		if (monitor != null && !monitor.isCanceled()) {
			monitor.subTask(msg);
			monitor.worked(1);
		}
	}

	private boolean isLocal() throws CoreException {
		return Platform.getOS().equals(toolChain.getProperty(IToolChain.ATTR_OS))
				&& Platform.getOSArch().equals(toolChain.getProperty(IToolChain.ATTR_ARCH));

	}

	/**
	 * Run 'meson' command.
	 *
	 * @param console
	 *            the build console to send messages to
	 * @param buildDir
	 *            abs. path
	 * @param srcDir
	 * @return a MultiStatus object, where .getCode() return the severity
	 * @throws CoreException
	 */
	private Process invokeMeson(IPath buildDir, IConsole console, IProgressMonitor monitor)
			throws CoreException, IOException {
		Assert.isLegal(buildDir.isAbsolute(), "buildDir");

		IPath cwd = project.getLocation();
		boolean buildDirExists = new File(buildDir.toOSString()).exists();
		if (buildDirExists) {
			cwd = buildDir;
		}

		// Make sure we have a toolchain file if cross
		if (crossCompileFile == null && !isLocal()) {
			IMesonCrossCompileManager manager = Activator.getService(IMesonCrossCompileManager.class);
			crossCompileFile = manager.getToolChainFileFor(toolChain);

			if (crossCompileFile == null) {
				// error
				console.getErrorStream().write(Messages.MesonBuildConfiguration_NoToolchainFile);
				return null;
			}
		}

		final List<String> argList = buildCommandline(buildDir);
		if (argList.isEmpty()) {
			// nothing to do
			return null;
		}
		// extract meson command
		final String cmd = argList.get(0);
		argList.remove(0);
		// Set the environment
		IEnvironmentVariable[] variables = config == null ? null
				: ManagedBuildManager.getEnvironmentVariableProvider().getVariables(config, true);
		String[] envp = null;
		ArrayList<String> envList = new ArrayList<>();
		if (variables != null) {
			for (int i = 0; i < variables.length; i++) {
				envList.add(variables[i].getName() + "=" + variables[i].getValue()); //$NON-NLS-1$
			}
			envp = envList.toArray(new String[envList.size()]);
		}
		// run meson..
		launcher.showCommand(true);
		return launcher.execute(new Path(cmd), argList.toArray(new String[argList.size()]), envp, cwd, monitor);
	}

	public boolean shouldGenerate() {
		boolean mustGenerate = false;
		final IFolder buildFolder = getBuildFolder();
		final File buildDir = buildFolder.getLocation().toFile();
		if (buildFolder.exists()) {
			final File makefile = new File(buildDir, getMakefileName());
			if (!buildDir.exists() || !makefile.exists()) {
				mustGenerate = true;
			}
		} else {
			mustGenerate = true;
		}
		return mustGenerate;
	}

	public String getMakefileName() {
		// load project properties..
		MesonPreferences prefs;
		try {
			prefs = ConfigurationManager.getInstance().getOrLoad(cfgd);
		} catch (CoreException ex) {
			// Auto-generated catch block
			ex.printStackTrace();
			return "build.ninja"; // default
		}
		AbstractOsPreferences osPrefs = AbstractOsPreferences.extractOsPreferences(prefs);
		// file generated by meson
		return osPrefs.getBackend().getMakefileName();
	}

	private float getMesonVersion() {
		try {
			final MesonPreferences prefs = ConfigurationManager.getInstance().getOrLoad(cfgd);
			final AbstractOsPreferences osPrefs = AbstractOsPreferences.extractOsPreferences(prefs);
			ProcessBuilder pb = new ProcessBuilder(getCommand(osPrefs), "-version");
			pb.redirectErrorStream(true);
			Process p = pb.start();
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				try {
					String[] parts = br.readLine().split("\\.");
					return Float.parseFloat(parts[0] + "." + parts[1]);
				} finally {
					br.close();
				}
			} finally {
				if (p.waitFor() != 0)
					throw new IllegalStateException("Command returned non-zero status.");
			}
		} catch (Exception e) {
			System.err.print("WARNING: Could not determine meson version, assuming 0.40.x");
			e.printStackTrace();
			return 0.40f;
		}

	}

	/**
	 * Build the command-line for meson. The first argument will be the
	 * meson-command.
	 *
	 * @throws CoreException
	 */
	private List<String> buildCommandline(IPath buildDir) throws CoreException {

		boolean needVerboseBuild = false;
		{
			final List<ILanguageSettingsProvider> lsps = ((ILanguageSettingsProvidersKeeper) cfgd)
					.getLanguageSettingProviders();
			for (ILanguageSettingsProvider lsp : lsps) {
				if (!needVerboseBuild
						&& ("org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser".equals(lsp.getId())
								|| "org.icemoon.meson.cdt.language.settings.providers.CmakeBuildOutputParser"
										.equals(lsp.getId()))) {
					needVerboseBuild = true;
					continue;
				}
			}
		}

		final MesonPreferences prefs = ConfigurationManager.getInstance().getOrLoad(cfgd);
		final AbstractOsPreferences osPrefs = AbstractOsPreferences.extractOsPreferences(prefs);

		List<String> args = new ArrayList<>();
		boolean buildDirExists = new File(buildDir.toOSString()).exists();
		List<ProjectOption> projectOptions = prefs.getProjectOptions();
		if (!buildDirExists) {

			args.add(getCommand(osPrefs));

			args.add("--buildtype="
					+ (prefs.getBuildType() == null ? "debug" : prefs.getBuildType().name().toLowerCase()));

			/* add general settings */
			if (prefs.isStrip())
				args.add("--strip");
			if (prefs.getUnity() != null)
				args.add("--unity=" + prefs.getUnity().name().toLowerCase());
			if (prefs.getDefaultLibrary() != null)
				args.add("--default-library=" + prefs.getDefaultLibrary().name().toLowerCase());
			if (prefs.isWarningsAsErrors())
				args.add("--werror");
			if (prefs.isStdSplit())
				args.add("--stdsplit");
			if (prefs.isErrorLogs())
				args.add("--errorlogs");
			if (prefs.getCrossFile() != null) {
				args.add("--cross-file");
				args.add(prefs.getCrossFile());
			}
			args.add("--warnlevel=" + prefs.getWarnLevel());

			appendProjectOptions(args, projectOptions);

			/* add settings for the operating system we are running under */
			appendAbstractOsPreferences(args, osPrefs);
			osPrefs.setGeneratedWith(osPrefs.getBackend());

			// tell meson where its script is located..
			args.add(buildDir.toOSString());
		} else {
			if (!projectOptions.isEmpty()) {
				if (getMesonVersion() >= 0.42f) {
					args.add(getCommand(osPrefs));
					args.add("configure");
				} else {
					args.add("mesonconf");
				}

				// TODO how do we reconfigure other stuff? maybe just output a message telling
				// user to upgrade Meson or clean the workspace. We would need to detect actual
				// changes to configuration though
				appendProjectOptions(args, projectOptions);
			}
		}

		return args;
	}

	/**
	 * Appends arguments common to all OS preferences. The first argument in the
	 * list will be replaced by the meson command from the specified preferences, if
	 * given.
	 *
	 * @param args
	 *            the list to append meson-arguments to.
	 * @param prefs
	 *            the generic OS preferences to convert and append.
	 * @throws CoreException
	 *             if unable to resolve the value of one or more variables
	 */
	private void appendAbstractOsPreferences(List<String> args, final AbstractOsPreferences prefs)
			throws CoreException {
		args.add("--backend=" + prefs.getBackend().name().toLowerCase());
		appendProjectOptions(args, prefs.getProjectOptions());
	}

	/**
	 * Appends arguments for the specified project options. Performs substitutions
	 * on variables found in a value of each define.
	 *
	 * @param args
	 *            the list to append meson-arguments to.
	 * @param defines
	 *            the meson project option to convert and append.
	 * @throws CoreException
	 *             if unable to resolve the value of one or more variables
	 */
	private void appendProjectOptions(List<String> args, final List<ProjectOption> defines) throws CoreException {
		final ICdtVariableManager mngr = CCorePlugin.getDefault().getCdtVariableManager();
		for (ProjectOption def : defines) {
			final StringBuilder sb = new StringBuilder("-D");
			sb.append(def.getName());
			sb.append('=');
			String expanded = mngr.resolveValue(def.getValue(), "", "", cfgd);
			sb.append(expanded);
			args.add(sb.toString());
		}
	}

	private String getCommand(AbstractOsPreferences prefs) throws CdtVariableException {
		if (!prefs.getUseDefaultCommand() && prefs.getCommand() != null && !prefs.getCommand().equals("")) {
			ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
			return varManager.resolveValue(prefs.getCommand(), "<undefined variable here, but CDT does not allow"
					+ " to pass it unexpanded; thus its name is lost>", null, cfgd);
		}
		return "meson";
	}

	private void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	public MultiStatus build(IProgressMonitor monitor, IConsole console, Map<String, String> env, String[] cli)
			throws CoreException {
		String[] args = new String[cli.length < 2 ? 0 : cli.length - 1];
		System.arraycopy(cli, 1, args, 0, args.length);
		String errMsg = null;

		String[] envp = null;
		ArrayList<String> envList = new ArrayList<>();
		if (env != null) {
			for (Map.Entry<String, String> en : env.entrySet()) {
				envList.add(en.getKey() + "=" + en.getValue()); //$NON-NLS-1$
			}
			envp = envList.toArray(new String[envList.size()]);
		}

		final Process proc = launcher.execute(new Path(cli[0]), args, envp, getBuildFolder().getLocation(), monitor);
		if (proc != null) {
			try {
				// Close the input of the process since we will never write to it
				proc.getOutputStream().close();
			} catch (IOException e) {
			}

			int state = launcher.waitAndRead(console.getOutputStream(), console.getErrorStream(),
					SubMonitor.convert(monitor, IProgressMonitor.UNKNOWN));
			if (state == ICommandLauncher.COMMAND_CANCELED) {
				throw new OperationCanceledException(launcher.getErrorMessage());
			}

			// check meson exit status
			final int exitValue = proc.exitValue();
			if (exitValue == 0) {
				// success
				return new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, null, null);
			} else {
				// meson had errors...
				errMsg = String.format("%1$s exited with status %2$d. See CDT global build console for details.",
						cli[0], exitValue);
				return new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, errMsg, null);
			}
		} else {
			// process start failed
			errMsg = launcher.getErrorMessage();
			return new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, errMsg, null);
		}

	}

	/**
	 * Recursively creates the folder hierarchy needed for the build output, if
	 * necessary. If the folder is created, its derived bit is set to true so the CM
	 * system ignores the contents. If the resource exists, respect the existing
	 * derived setting.
	 *
	 * @param folder
	 *            a folder, somewhere below the project root
	 */
	// private void createFolder(IFolder folder) throws CoreException {
	// if (!folder.exists()) {
	// // Make sure that parent folders exist
	// IContainer parent = folder.getParent();
	// if (parent instanceof IFolder && !parent.exists()) {
	// createFolder((IFolder) parent);
	// }
	//
	// // Now make the requested folder
	// try {
	// folder.create(IResource.DERIVED, true, monitor);
	// } catch (CoreException e) {
	// if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
	// folder.refreshLocal(IResource.DEPTH_ZERO, monitor);
	// else
	// throw e;
	// }
	// }
	// }
}
