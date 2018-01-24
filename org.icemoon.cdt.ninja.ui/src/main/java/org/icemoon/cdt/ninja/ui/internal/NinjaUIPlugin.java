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
package org.icemoon.cdt.ninja.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.icemoon.cdt.ninja.ui.internal.editor.ColorManager;
import org.icemoon.cdt.ninja.ui.internal.editor.INinjaDocumentProvider;
import org.icemoon.cdt.ninja.ui.internal.editor.NinjaDocumentProvider;
import org.icemoon.cdt.ninja.ui.internal.editor.WorkingCopyManager;
import org.osgi.framework.BundleContext;

/**
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class NinjaUIPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.icemoon.cdt.ninja.ui"; //$NON-NLS-1$

	// The shared instance.
	private static NinjaUIPlugin plugin;

	/**
	 * Returns the shared instance.
	 */
	public static NinjaUIPlugin getDefault() {
		return plugin;
	}

	private ResourceBundle resourceBundle;
	private NinjaDocumentProvider fMakefileDocumentProvider;
	private WorkingCopyManager fWorkingCopyManager;

	/**
	 * The constructor.
	 */
	public NinjaUIPlugin() {
		try {
			resourceBundle = ResourceBundle.getBundle("org.icemoon.cdt.ninja.ui.internal.NinjaResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
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
	
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = NinjaUIPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public synchronized INinjaDocumentProvider getNinjaDocumentProvider() {
		if (fMakefileDocumentProvider == null) {
			fMakefileDocumentProvider=  new NinjaDocumentProvider();
		}
		return fMakefileDocumentProvider;
	}

	public synchronized IWorkingCopyManager getWorkingCopyManager() {
		if (fWorkingCopyManager == null) {
			INinjaDocumentProvider provider= getNinjaDocumentProvider();
			fWorkingCopyManager= new WorkingCopyManager(provider);
		}
		return fWorkingCopyManager;
	}

	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}

	/**
	 * Returns the active workbench window or <code>null</code> if none
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	* Utility method with conventions
	*/
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		log(s);
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message = null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}

	/**
	* Utility method with conventions
	*/
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		log(t);
		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException) t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message = null;
			}
		} else {
			status = new Status(IStatus.ERROR, PLUGIN_ID, -1, "Internal Error: ", t); //$NON-NLS-1$
		}
		ErrorDialog.openError(shell, title, message, status);
	}

	/**
	 * Returns a combined preference store, this store is read-only.
	 *
	 * @return the combined preference store
	 */
	public IPreferenceStore getCombinedPreferenceStore() {
		IPreferenceStore[] stores = new IPreferenceStore[2];
		stores[0] = getPreferenceStore();
		stores[1] = EditorsUI.getPreferenceStore();
		ChainedPreferenceStore chainedStore = new ChainedPreferenceStore(stores);
		return chainedStore;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the preference color, identified by the given preference.
	 */
	public static Color getPreferenceColor(String key) {
		return ColorManager.getDefault().getColor(PreferenceConverter.getColor(getDefault().getPreferenceStore(), key));
	}

	/**
	 * Returns the Unique identifier for this plugin.
	 */
	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}

	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.OK, e.getMessage(), e);
		log(status);
	}
}
