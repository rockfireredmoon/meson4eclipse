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
package org.icemoon.meson;

import java.text.MessageFormat;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Martin Weber 
 * @author Emerald Icemoon
 */
public class MesonPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.icemoon.meson"; //$NON-NLS-1$

	// The shared instance.
	private static MesonPlugin plugin;

	/**
	 * The constructor.
	 */
	public MesonPlugin() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
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
	public static MesonPlugin getDefault() {
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
		// ResourceBundle bundle = MesonPlugin.getDefault().getResourceBundle();
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
}