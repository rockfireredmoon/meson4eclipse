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
package org.icemoon.cdt.meson.core.internal.settings;

import java.util.WeakHashMap;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.runtime.CoreException;

/**
 * Associates {@link ICConfigurationDescription} objects with our
 * MesonPreferences objects in order to avoid redundant de-serialization from
 * storage.
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public final class ConfigurationManager {
	private static ConfigurationManager instance;

	/** caches MesonPreferences by ICConfigurationDescription.ID */
	private WeakHashMap<String, MesonPreferences> map = new WeakHashMap<String, MesonPreferences>(2);

	/**
	 * Singleton constructor.
	 */
	private ConfigurationManager() {
	}

	/**
	 * Gets the singleton instance.
	 */
	public static synchronized ConfigurationManager getInstance() {
		if (instance == null)
			instance = new ConfigurationManager();
		return instance;
	}

	// /**
	// * Associates the specified {@code ICConfigurationDescription} from the CDT
	// * data model with the specified MesonPreferences object and stores it for
	// * later retrieval. The association will be automatically removed as soon as
	// * there is no longer a hard reference to the ICConfigurationDescription.
	// *
	// * @throws NullPointerException
	// * if prefs is {@code null}
	// */
	// public void put(ICConfigurationDescription cfgd, MesonPreferences prefs) {
	// if (prefs == null) {
	// throw new NullPointerException("prefs");
	// }
	//
	// map.put(cfgd, prefs);
	// }

	/**
	 * Gets the {@code MesonPreferences} object associated with the specified
	 * {@code ICConfigurationDescription}.
	 *
	 * @return the stored {@code MesonPreferences} object, or {@code null} if this
	 *         object contains no mapping for the configuration description
	 */
	public MesonPreferences get(ICConfigurationDescription cfgd) {
		return map.get(cfgd.getId());
	}

	/**
	 * Tries to get the {@code MesonPreferences} object associated with the
	 * specified {@code ICConfigurationDescription}. If no {@code MesonPreferences}
	 * object is found, a new one is created.
	 *
	 * @return the stored {@code MesonPreferences} object, or a newly created one if
	 *         this object contains no mapping for the configuration description.
	 */
	public MesonPreferences getOrCreate(ICConfigurationDescription cfgd) {
		MesonPreferences pref = map.get(cfgd.getId());
		if (pref == null) {
			pref = new MesonPreferences();
			map.put(cfgd.getId(), pref);
		}
		return pref;
	}

	/**
	 * Tries to get the {@code MesonPreferences} object associated with the
	 * specified {@code ICConfigurationDescription}. If no {@code MesonPreferences}
	 * object is found, a new one is created, then loaded from its storage via
	 * {@link MesonPreferences#loadFromStorage}.
	 *
	 * @return the stored {@code MesonPreferences} object, or a freshly loaded one
	 *         if this object contains no mapping for the configuration description.
	 * @throws CoreException
	 *             if {@link ICConfigurationDescription#getStorage} throws a
	 *             CoreException.
	 */
	public MesonPreferences getOrLoad(ICConfigurationDescription cfgd) throws CoreException {
		MesonPreferences pref = map.get(cfgd.getId());
		if (pref == null) {
			pref = new MesonPreferences();
			ICStorageElement storage = cfgd.getStorage(MesonPreferences.CFG_STORAGE_ID, false);
			pref.loadFromStorage(storage);
			map.put(cfgd.getId(), pref);
		}
		return pref;
	}
}
