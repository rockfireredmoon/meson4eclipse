package org.icemoon.cdt.ninja.core.language.settings.providers;

import java.io.File;
import java.util.List;

import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.icemoon.cdt.ninja.core.NinjaPlugin;

public abstract class AbstractParser extends LanguageSettingsSerializableProvider
		implements ILanguageSettingsProvider, ICListenerAgent, ICBuildOutputParser {

	private static final ILog log = NinjaPlugin.getDefault().getLog();

	/**
	 * default regex string used for version pattern matching.
	 *
	 * @see #isVersionPatternEnabled()
	 */
	private static final String DEFALT_VERSION_PATTERN = "-?\\d+(\\.\\d+)*";
	/** storage key for version pattern */
	private static final String ATTR_PATTERN = "vPattern";
	/** storage key for version pattern enabled */
	private static final String ATTR_PATTERN_ENABLED = "vPatternEnabled";

	private String markerId;
	private String parserId;

	/**
	 * Storage to keep settings entries
	 */
	protected PerConfigLanguageSettingsStorage storage = new PerConfigLanguageSettingsStorage();

	protected ICConfigurationDescription currentCfgDescription;

	/**
	 * last known working tool detector and its tool option parsers or {@code null},
	 * if unknown (to speed up parsing)
	 */
	protected ParserDetection.DetectorWithMethod lastDetector;

	protected AbstractParser(String markerId, String parserId) {
		this.markerId = markerId;
		this.parserId = parserId;
	}

	protected abstract void tryParse(boolean initializingWorkbench) throws CoreException;

	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc,
			String languageId) {
		if (cfgDescription == null || rc == null) {
			// speed up, we do not provide global (workspace) lang settings..
			return null;
		}
		return storage.getSettingEntries(cfgDescription, rc, languageId);
	}

	/*-
	 * interface ICBuildOutputParser
	 */
	@Override
	public void shutdown() {
		try {
			if (currentCfgDescription != null)
				tryParse(false);
		} catch (CoreException ex) {
			log.log(new Status(IStatus.ERROR, NinjaPlugin.PLUGIN_ID, "shutdown()", ex));
		}
		// RELEASE resources for garbage collector
		currentCfgDescription = null;
	}

	/**
	 * Gets whether the parser will also try to detect compiler command that have a
	 * trailing version-string in their name. If enabled, this parser will also try
	 * to match for example {@code gcc-4.6} or {@code gcc-4.6.exe} if none of the
	 * other patterns matched.
	 *
	 * @return <code>true</code> version pattern matching in command names is
	 *         enabled, otherwise <code>false</code>
	 */
	public boolean isVersionPatternEnabled() {
		return getPropertyBool(ATTR_PATTERN_ENABLED);
	}

	/**
	 * Sets whether version pattern matching is performed.
	 *
	 * @see #isVersionPatternEnabled()
	 */
	public void setVersionPatternEnabled(boolean enabled) {
		setPropertyBool(ATTR_PATTERN_ENABLED, enabled);
	}

	/**
	 * Gets the regex pattern string used for version pattern matching.
	 *
	 * @see #isVersionPatternEnabled()
	 */
	public String getVersionPattern() {
		String val = properties.get(ATTR_PATTERN);
		if (val == null || val.isEmpty()) {
			// provide a default pattern
			val = DEFALT_VERSION_PATTERN;
		}
		return val;
	}

	/**
	 * Sets the regex pattern string used for version pattern matching.
	 *
	 * @see #isVersionPatternEnabled()
	 */
	public void setVersionPattern(String versionPattern) {
		if (versionPattern == null || versionPattern.isEmpty() || versionPattern.equals(DEFALT_VERSION_PATTERN)) {
			// do not store default pattern
			properties.remove(ATTR_PATTERN);
		} else {
			setProperty(ATTR_PATTERN, versionPattern);
		}
	}

	/*-
	 * interface ICBuildOutputParser
	 */
	@Override
	public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker)
			throws CoreException {
		currentCfgDescription = cfgDescription;
	}

	/**
	 * Invoked for each line in the build output.
	 */
	// interface ICBuildOutputParser
	@Override
	public boolean processLine(String line) {
		// nothing to do, we parse later...
		return false;
	}

	@Override
	public LanguageSettingsStorage copyStorage() {
		if (currentCfgDescription == null)
			return null;
		TimestampedLanguageSettingsStorage st = storage.getSettingsForConfig(currentCfgDescription);
		return st.clone();
	}

	/**
	 * Overridden to misuse this to populate the {@link #getSettingEntries setting
	 * entries} on startup.<br>
	 * {@inheritDoc}
	 */
	@Override
	public void registerListener(ICConfigurationDescription cfgDescription) {
		if (cfgDescription != null) {
			// per-project or if the user just added this provider on the provider tab
			currentCfgDescription = cfgDescription;
			try {
				tryParse(true);
			} catch (CoreException ex) {
				log.log(new Status(IStatus.ERROR, NinjaPlugin.PLUGIN_ID, "registerListener()", ex));
			}
		} else {
			// per workspace (to populate on startup)
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = workspaceRoot.getProjects();
			CCorePlugin ccp = CCorePlugin.getDefault();
			// parse JSOn file for any opened project that has a ScannerConfigNature...
			for (IProject project : projects) {
				try {
					if (project.isOpen() && project.hasNature(ScannerConfigNature.NATURE_ID)) {
						ICProjectDescription projectDescription = ccp.getProjectDescription(project, false);
						if (projectDescription != null) {
							ICConfigurationDescription activeConfiguration = projectDescription
									.getActiveConfiguration();
							if (activeConfiguration instanceof ILanguageSettingsProvidersKeeper) {
								final List<ILanguageSettingsProvider> lsps = ((ILanguageSettingsProvidersKeeper) activeConfiguration)
										.getLanguageSettingProviders();
								for (ILanguageSettingsProvider lsp : lsps) {
									if (parserId.equals(lsp.getId())) {
										currentCfgDescription = activeConfiguration;
										tryParse(true);
										break;
									}
								}
							}
						}
					}
				} catch (CoreException ex) {
					log.log(new Status(IStatus.ERROR, NinjaPlugin.PLUGIN_ID, "registerListener()", ex));
				}
			}
		}
		// RELEASE resources for garbage collector
		currentCfgDescription = null;
	}

	/*-
	 * @see org.eclipse.cdt.core.language.settings.providers.ICListenerAgent#unregisterListener()
	 */
	@Override
	public void unregisterListener() {
	}

	protected void createMarker(IFile file, String message) throws CoreException {
		IMarker marker;
		try {
			marker = file.createMarker(markerId);
		} catch (CoreException ex) {
			// resource is not (yet) known by the workbench
			file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
			try {
				marker = file.createMarker(markerId);
			} catch (CoreException ex2) {
				// resource is not known by the workbench, use project instead of file
				marker = file.getProject().createMarker(markerId);
			}
		}
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
		marker.setAttribute(IMarker.MESSAGE, message);
	}

	/**
	 * Determines the parser detector that can parse the specified command-line.<br>
	 * Tries to be fast: That is, it tries the last known working detector first and
	 * will perform expensive detection required under windows only if needed.
	 *
	 * @param line
	 *            the command line to process
	 *
	 * @return {@code null} if none of the detectors matches the tool name in the
	 *         specified command-line string. Otherwise, if the tool name matches, a
	 *         {@code ParserDetectionResult} holding the remaining command-line
	 *         string (without the portion that matched) is returned.
	 */
	protected ParserDetection.ParserDetectionResult fastDetermineDetector(String line) {
		// try last known matching detector first...
		if (lastDetector != null) {
			String remaining = null;
			final ParserDetection.ParserDetector detector = lastDetector.getDetector();
			switch (lastDetector.getHow()) {
			case BASENAME:
				remaining = detector.basenameMatches(line);
				break;
			case WITH_EXTENSION:
				remaining = ((ParserDetection.ParserDetectorExt) detector).basenameWithExtensionMatches(line);
				break;
			case WITH_VERSION:
				if (isVersionPatternEnabled()) {
					remaining = detector.basenameWithVersionMatches(line, getVersionPattern());
				}
				break;
			case WITH_VERSION_EXTENSION:
				if (isVersionPatternEnabled()) {
					remaining = ((ParserDetection.ParserDetectorExt) detector)
							.basenameWithVersionAndExtensionMatches(line, getVersionPattern());
				}
				break;
			default:
				break;
			}
			if (remaining != null) {
				return new ParserDetection.ParserDetectionResult(lastDetector, remaining);
			} else {
				lastDetector = null; // invalidate last working detector
			}
		}

		// no working detector found, determine a new one...
		String versionPattern = isVersionPatternEnabled() ? getVersionPattern() : null;
		ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector(line, versionPattern,
				File.separatorChar == '\\');
		if (result != null) {
			// cache last working detector
			lastDetector = result.getDetectorWithMethod();
		}
		return result;
	}
}
