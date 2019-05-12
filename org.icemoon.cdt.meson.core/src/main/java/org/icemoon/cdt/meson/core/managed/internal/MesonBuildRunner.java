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
package org.icemoon.cdt.meson.core.managed.internal;

import java.util.Map;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.managedbuilder.core.AbstractBuildRunner;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.ExternalBuildRunner;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues;
import org.eclipse.cdt.managedbuilder.macros.IReservedMacroNameSupplier;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.icemoon.cdt.meson.core.internal.MesonHelper;
import org.osgi.framework.Version;

/**
 * An ExternalBuildRunner that injects the build tool command to use and some of
 * its arguments into the build. Necessary since CDT does not allow
 * {@code IConfigurationBuildMacroSupplier}s for a Builder (only for
 * tool-chains).
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonBuildRunner extends ExternalBuildRunner {
	public final static String BUILD_RUNNER_ID = "org.icemoon.cdt.meson.core.genscriptbuilder";

	/*-
	 * @see org.eclipse.cdt.managedbuilder.core.ExternalBuildRunner#invokeBuild(int, org.eclipse.core.resources.IProject, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.core.IBuilder, org.eclipse.cdt.core.resources.IConsole, org.eclipse.cdt.core.IMarkerGenerator, org.eclipse.core.resources.IncrementalProjectBuilder, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean invokeBuild(int kind, IProject project, IConfiguration configuration, IBuilder builder,
			IConsole console, IMarkerGenerator markerGenerator, IncrementalProjectBuilder projectBuilder,
			IProgressMonitor monitor) throws CoreException {

		System.out.println("XXXX invokeBuild " + kind + " [" + project + "] / " + builder.getBaseId());
		/*
		 * wrap the passed-in builder into one that gets its build command from the
		 * Cmake-backend. First do a sanity check.
		 */
		if (builder.getBaseId().equals(BUILD_RUNNER_ID)) {
			final ICConfigurationDescription cfgd = ManagedBuildManager.getDescriptionForConfiguration(configuration);

			if (kind == IncrementalProjectBuilder.CLEAN_BUILD) {
				// avoid calling 'rm -rf' if it is a clean build and the build dir was
				// deleted
				final IPath builderCWD = cfgd.getBuildSetting().getBuilderCWD();
				IFile cwd = ResourcesPlugin.getWorkspace().getRoot().getFile(builderCWD);
				final IPath location = cwd.getLocation();
				if (location == null || !location.toFile().exists()) {
					return true; // is clean
				}
			}

			builder = new MesonBuildToolInjectorBuilder(builder,
					new MesonHelper(configuration.getName(), null, null, null, project, configuration));
		}
		try {
			return super.invokeBuild(kind, project, configuration, builder, console, markerGenerator, projectBuilder,
					monitor);
		} catch (Exception e) {
			if (e instanceof CoreException)
				throw (CoreException) e;
			else
				throw new RuntimeException(e);
		}
	}

	////////////////////////////////////////////////////////////////////
	// inner classes
	////////////////////////////////////////////////////////////////////

	/**
	 * @author Martin Weber @author Emerald Icemoon
	 */
	private static class MesonBuildToolInjectorBuilder implements IBuilder {
		private final IBuilder delegate;
		private final MesonHelper helper;

		/**
		 * @param delegate
		 *            the builder we delegate most of the methods to
		 * @param mesonBuildTool
		 *            the buildscript processor command to inject (e.g. 'ninja')
		 * @param backend
		 *            the meson backend that generated the build scripts.
		 */
		public MesonBuildToolInjectorBuilder(IBuilder delegate, MesonHelper helper) {
			this.delegate = delegate;
			this.helper = helper;
		}

		@Override
		public IPath getBuildCommand() {
			try {
				return new Path(helper.getBuildCommand(delegate.getBuildCommand().toString()));
			} catch (CoreException e) {
				throw new IllegalStateException("Failed to get build command.");
			}
		}

		@Override
		public String getBuildArguments() {
			try {
				return helper.getBuildArguments(delegate.getBuildArguments());
			} catch (CoreException e) {
				throw new IllegalStateException("Failed to get build command.");
			}
		}

		@Override
		public String getArguments() {
			return delegate.getArguments();
		}

		@Override
		public String getCommand() {
			return this.delegate.getCommand();
		}

		@Override
		public String getId() {
			return this.delegate.getId();
		}

		@Override
		public String getName() {
			return this.delegate.getName();
		}

		@Override
		public String getBaseId() {
			return this.delegate.getBaseId();
		}

		@Override
		public Version getVersion() {
			return this.delegate.getVersion();
		}

		@Override
		public void setVersion(Version version) {
			this.delegate.setVersion(version);
		}

		@Override
		public String getManagedBuildRevision() {
			return this.delegate.getManagedBuildRevision();
		}

		@Override
		public IOption createOption(IOption superClass, String Id, String name, boolean isExtensionElement) {
			return this.delegate.createOption(superClass, Id, name, isExtensionElement);
		}

		@Override
		public void setBuildAttribute(String name, String value) throws CoreException {
			this.delegate.setBuildAttribute(name, value);
		}

		@Override
		public String getBuildAttribute(String name, String defaultValue) {
			return this.delegate.getBuildAttribute(name, defaultValue);
		}

		@Override
		public boolean isAutoBuildEnable() {
			return this.delegate.isAutoBuildEnable();
		}

		@Override
		public IPath getBuildLocation() {
			return this.delegate.getBuildLocation();
		}

		@Override
		public void setAutoBuildEnable(boolean enabled) throws CoreException {
			this.delegate.setAutoBuildEnable(enabled);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void setBuildLocation(IPath location) throws CoreException {
			this.delegate.setBuildLocation(location);
		}

		@Override
		public String getAutoBuildTarget() {
			return this.delegate.getAutoBuildTarget();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void setAutoBuildTarget(String target) throws CoreException {
			this.delegate.setAutoBuildTarget(target);
		}

		@Override
		public boolean isStopOnError() {
			return this.delegate.isStopOnError();
		}

		@Override
		public void setStopOnError(boolean on) throws CoreException {
			this.delegate.setStopOnError(on);
		}

		@Override
		public boolean isIncrementalBuildEnabled() {
			return this.delegate.isIncrementalBuildEnabled();
		}

		@Override
		public boolean supportsStopOnError(boolean on) {
			return this.delegate.supportsStopOnError(on);
		}

		@Override
		public void setIncrementalBuildEnable(boolean enabled) throws CoreException {
			this.delegate.setIncrementalBuildEnable(enabled);
		}

		@Override
		public int getParallelizationNum() {
			return this.delegate.getParallelizationNum();
		}

		@Override
		public void removeOption(IOption option) {
			this.delegate.removeOption(option);
		}

		@Override
		public String getIncrementalBuildTarget() {
			return this.delegate.getIncrementalBuildTarget();
		}

		@SuppressWarnings("deprecation")
		@Override
		public IOption getOption(String id) {
			return this.delegate.getOption(id);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void setIncrementalBuildTarget(String target) throws CoreException {
			this.delegate.setIncrementalBuildTarget(target);
		}

		@Override
		public void setParallelizationNum(int jobs) throws CoreException {
			this.delegate.setParallelizationNum(jobs);
		}

		@Override
		public boolean isFullBuildEnabled() {
			return this.delegate.isFullBuildEnabled();
		}

		@Override
		public void setFullBuildEnable(boolean enabled) throws CoreException {
			this.delegate.setFullBuildEnable(enabled);
		}

		@Override
		public String getFullBuildTarget() {
			return this.delegate.getFullBuildTarget();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void setFullBuildTarget(String target) throws CoreException {
			this.delegate.setFullBuildTarget(target);
		}

		@Override
		public boolean supportsParallelBuild() {
			return this.delegate.supportsParallelBuild();
		}

		@Override
		public String getCleanBuildTarget() {
			return this.delegate.getCleanBuildTarget();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void setCleanBuildTarget(String target) throws CoreException {
			this.delegate.setCleanBuildTarget(target);
		}

		@Override
		public IOption getOptionById(String id) {
			return this.delegate.getOptionById(id);
		}

		@Override
		public boolean isParallelBuildOn() {
			return this.delegate.isParallelBuildOn();
		}

		@Override
		public boolean isCleanBuildEnabled() {
			return this.delegate.isCleanBuildEnabled();
		}

		@Override
		public void setCleanBuildEnable(boolean enabled) throws CoreException {
			this.delegate.setCleanBuildEnable(enabled);
		}

		@Override
		public void setParallelBuildOn(boolean on) throws CoreException {
			this.delegate.setParallelBuildOn(on);
		}

		@Override
		public boolean isDefaultBuildCmd() {
			return this.delegate.isDefaultBuildCmd();
		}

		@Override
		public void setUseDefaultBuildCmd(boolean on) throws CoreException {
			this.delegate.setUseDefaultBuildCmd(on);
		}

		@Override
		public IOption getOptionBySuperClassId(String id) {
			return this.delegate.getOptionBySuperClassId(id);
		}

		@Override
		@SuppressWarnings("deprecation")
		public void setBuildCommand(IPath command) throws CoreException {
			this.delegate.setBuildCommand(command);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void setBuildArguments(String args) throws CoreException {
			this.delegate.setBuildArguments(args);
		}

		@Override
		public String[] getErrorParsers() {
			return this.delegate.getErrorParsers();
		}

		@Override
		public void setErrorParsers(String[] parsers) throws CoreException {
			this.delegate.setErrorParsers(parsers);
		}

		@Override
		public Map<String, String> getExpandedEnvironment() throws CoreException {
			return this.delegate.getExpandedEnvironment();
		}

		@Override
		public IOption[] getOptions() {
			return this.delegate.getOptions();
		}

		@Override
		public Map<String, String> getEnvironment() {
			return this.delegate.getEnvironment();
		}

		@Override
		public void setEnvironment(Map<String, String> env) throws CoreException {
			this.delegate.setEnvironment(env);
		}

		@Override
		public boolean appendEnvironment() {
			return this.delegate.appendEnvironment();
		}

		@Override
		public void setAppendEnvironment(boolean append) throws CoreException {
			this.delegate.setAppendEnvironment(append);
		}

		@Override
		public boolean isManagedBuildOn() {
			return this.delegate.isManagedBuildOn();
		}

		@Override
		public void setManagedBuildOn(boolean on) throws CoreException {
			this.delegate.setManagedBuildOn(on);
		}

		@Override
		public IOptionCategory[] getChildCategories() {
			return this.delegate.getChildCategories();
		}

		@Override
		public boolean supportsBuild(boolean managed) {
			return this.delegate.supportsBuild(managed);
		}

		@Override
		public void addOptionCategory(IOptionCategory category) {
			this.delegate.addOptionCategory(category);
		}

		@Override
		public IOptionCategory getOptionCategory(String id) {
			return this.delegate.getOptionCategory(id);
		}

		@Override
		public void createOptions(IHoldsOptions superClass) {
			this.delegate.createOptions(superClass);
		}

		@Override
		public IOption getOptionToSet(IOption option, boolean adjustExtension) throws BuildException {
			return this.delegate.getOptionToSet(option, adjustExtension);
		}

		@Override
		public boolean needsRebuild() {
			return this.delegate.needsRebuild();
		}

		@Override
		public void setRebuildState(boolean rebuild) {
			this.delegate.setRebuildState(rebuild);
		}

		@Override
		@SuppressWarnings("deprecation")
		public IConfigurationElement getBuildFileGeneratorElement() {
			return this.delegate.getBuildFileGeneratorElement();
		}

		@Override
		public IManagedBuilderMakefileGenerator getBuildFileGenerator() {
			return this.delegate.getBuildFileGenerator();
		}

		@Override
		public String getErrorParserIds() {
			return this.delegate.getErrorParserIds();
		}

		@Override
		public String[] getErrorParserList() {
			return this.delegate.getErrorParserList();
		}

		@Override
		public IToolChain getParent() {
			return this.delegate.getParent();
		}

		@Override
		public IBuilder getSuperClass() {
			return this.delegate.getSuperClass();
		}

		@Override
		public String getUnusedChildren() {
			return this.delegate.getUnusedChildren();
		}

		@Override
		public boolean isAbstract() {
			return this.delegate.isAbstract();
		}

		@Override
		public boolean isDirty() {
			return this.delegate.isDirty();
		}

		@Override
		public boolean isExtensionElement() {
			return this.delegate.isExtensionElement();
		}

		@Override
		public void setArguments(String makeArgs) {
			this.delegate.setArguments(makeArgs);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void setBuildFileGeneratorElement(IConfigurationElement element) {
			this.delegate.setBuildFileGeneratorElement(element);
		}

		@Override
		public void setCommand(String command) {
			this.delegate.setCommand(command);
		}

		@Override
		public void setDirty(boolean isDirty) {
			this.delegate.setDirty(isDirty);
		}

		@Override
		public void setErrorParserIds(String ids) {
			this.delegate.setErrorParserIds(ids);
		}

		@Override
		public void setIsAbstract(boolean b) {
			this.delegate.setIsAbstract(b);
		}

		@Override
		public String getVersionsSupported() {
			return this.delegate.getVersionsSupported();
		}

		@Override
		public String getConvertToId() {
			return this.delegate.getConvertToId();
		}

		@Override
		public void setVersionsSupported(String versionsSupported) {
			this.delegate.setVersionsSupported(versionsSupported);
		}

		@Override
		public void setConvertToId(String convertToId) {
			this.delegate.setConvertToId(convertToId);
		}

		@Override
		public IFileContextBuildMacroValues getFileContextBuildMacroValues() {
			return this.delegate.getFileContextBuildMacroValues();
		}

		@Override
		public String getBuilderVariablePattern() {
			return this.delegate.getBuilderVariablePattern();
		}

		@Override
		public boolean isVariableCaseSensitive() {
			return this.delegate.isVariableCaseSensitive();
		}

		@Override
		public String[] getReservedMacroNames() {
			return this.delegate.getReservedMacroNames();
		}

		@Override
		public IReservedMacroNameSupplier getReservedMacroNameSupplier() {
			return this.delegate.getReservedMacroNameSupplier();
		}

		@Override
		public CBuildData getBuildData() {
			return this.delegate.getBuildData();
		}

		@Override
		public boolean isCustomBuilder() {
			return this.delegate.isCustomBuilder();
		}

		@Override
		public boolean supportsCustomizedBuild() {
			return this.delegate.supportsCustomizedBuild();
		}

		@Override
		public boolean keepEnvironmentVariablesInBuildfile() {
			return this.delegate.keepEnvironmentVariablesInBuildfile();
		}

		@Override
		public void setKeepEnvironmentVariablesInBuildfile(boolean keep) {
			this.delegate.setKeepEnvironmentVariablesInBuildfile(keep);
		}

		@Override
		public boolean canKeepEnvironmentVariablesInBuildfile() {
			return this.delegate.canKeepEnvironmentVariablesInBuildfile();
		}

		@Override
		public void setBuildPath(String path) {
			this.delegate.setBuildPath(path);
		}

		@Override
		public String getBuildPath() {
			return this.delegate.getBuildPath();
		}

		@Override
		public boolean isInternalBuilder() {
			return this.delegate.isInternalBuilder();
		}

		@Override
		public boolean matches(IBuilder builder) {
			return this.delegate.matches(builder);
		}

		@Override
		public boolean isSystemObject() {
			return this.delegate.isSystemObject();
		}

		@Override
		public String getUniqueRealName() {
			return this.delegate.getUniqueRealName();
		}

		@Override
		public ICommandLauncher getCommandLauncher() {
			return this.delegate.getCommandLauncher();
		}

		@Override
		public AbstractBuildRunner getBuildRunner() throws CoreException {
			return this.delegate.getBuildRunner();
		}

	}
}
