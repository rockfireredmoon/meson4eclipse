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
package org.icemoon.cdt.meson.ui.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class MesonPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.icemoon.cdt.meson.ui"; //$NON-NLS-1$

	// The shared instance.
	private static MesonPlugin plugin;

	/**
	 * Returns the shared instance.
	 */
	public static MesonPlugin getDefault() {
		return plugin;
	}

	/**
	 * The constructor.
	 */
	public MesonPlugin() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		if (!PLUGIN_ID.equals(this.getBundle().getSymbolicName()))
			throw new RuntimeException("BUG: PLUGIN_ID does not match Bundle-SymbolicName");
		plugin = this;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}
}
