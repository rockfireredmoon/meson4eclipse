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
package org.icemoon.cdt.meson.core;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.icemoon.cdt.meson.core.internal.MesonCrossCompileManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.icemoon.cdt.meson.core"; //$NON-NLS-1$

	// The shared instance.
	private static Activator plugin;
	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/**
	 * The constructor.
	 */
	public Activator() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		context.registerService(IMesonCrossCompileManager.class, new MesonCrossCompileManager(), null);
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
	public static Activator getDefault() {
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
		// ResourceBundle bundle = Activator.getDefault().getResourceBundle();
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
}
