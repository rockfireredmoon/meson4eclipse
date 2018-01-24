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
package org.icemoon.cdt.ninja.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider;
import org.eclipse.cdt.make.internal.core.BuildInfoFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class NinjaPlugin extends AbstractUIPlugin {

	private static class FileStoreReaderProvider implements IMakefileReaderProvider {

		private final String fEncoding;

		private FileStoreReaderProvider(String encoding) {
			fEncoding = encoding != null ? encoding : ResourcesPlugin.getEncoding();
		}

		@Override
		public Reader getReader(URI fileURI) throws IOException {
			try {
				final IFileStore store = EFS.getStore(fileURI);
				final IFileInfo info = store.fetchInfo();
				if (!info.exists() || info.isDirectory())
					throw new IOException();

				return new InputStreamReader(store.openInputStream(EFS.NONE, null), fEncoding);
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
				throw new IOException(e.getMessage());
			}
		}
	}

	public static final String PLUGIN_ID = "org.icemoon.cdt.ninja.core"; //$NON-NLS-1$

	// The shared instance.
	private static NinjaPlugin plugin;
	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/**
	 * The constructor.
	 */
	public NinjaPlugin() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		NinjaPlugin.context = context;
		super.start(context);
		if (!PLUGIN_ID.equals(this.getBundle().getSymbolicName()))
			throw new RuntimeException("BUG: PLUGIN_ID does not match Bundle-SymbolicName");
		plugin = this;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static NinjaPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 *
	 * @param key
	 *            the message key
	 * @return the resource bundle message
	 */
	public static String getResourceString(String key) {
		// ResourceBundle bundle = NinjaPlugin.getDefault().getResourceBundle();
		// try {
		// return bundle.getString(key);
		// } catch (MissingResourceException e) {
		// return key;
		// }
		return key;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 *
	 * @param key
	 *            the message key
	 * @param args
	 *            an array of substitution strings
	 * @return the resource bundle message
	 */
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[]) args);
	}

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void log(Throwable e) {
		if (e instanceof CoreException) {
			plugin.getLog().log(((CoreException) e).getStatus());
		} else {
			plugin.getLog().log(errorStatus(e.getLocalizedMessage(), e));
		}
	}

	public static IStatus errorStatus(String message, Throwable cause) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message, cause);
	}

	public static <T> T getService(Class<T> service) {
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	public static IMakefile createMakefile(IFileStore file) throws CoreException {
		return createMakefile(file.toURI(), (String) null);
	}

	static IMakefile createMakefile(IFileStore file, String encoding) throws CoreException {
		return createMakefile(file.toURI(), encoding);
	}

	static IMakefile createMakefile(URI fileURI, String encoding) {
		return createMakefile(fileURI, new FileStoreReaderProvider(encoding));
	}

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	public static IMakeBuilderInfo createBuildInfo(Preferences prefs, String builderID, boolean useDefaults) {
		return BuildInfoFactory.create(prefs, builderID, useDefaults);
	}

	public static IMakeBuilderInfo createBuildInfo(IProject project, String builderID) throws CoreException {
		return BuildInfoFactory.create(project, builderID);
	}

	public static IMakeBuilderInfo createBuildInfo(Map<String, String> args, String builderID) {
		return BuildInfoFactory.create(args, builderID);
	}

	/**
	 * Create an IMakefile using the given IMakefileReaderProvider to fetch
	 * contents by name.
	 *
	 * @param fileURI URI of main file
	 * @param makefileReaderProvider may be <code>null</code> for EFS IFileStore reading
	 */
	public static IMakefile createMakefile(URI fileURI,IMakefileReaderProvider makefileReaderProvider) {
		IMakefile makefile;
			NinjaFile posix = new NinjaFile();
			try {
				posix.parse(fileURI, makefileReaderProvider);
			} catch (IOException e) {
			}
			makefile = posix;
		return makefile;
	}
}
